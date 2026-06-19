package io.synapse.ai.features.add_pdf.presentation.coordinator

import io.synapse.ai.core.ui.model.toUiModel
import io.synapse.ai.domains.auth.repository.IAuthRepository
import io.synapse.ai.domains.study.model.PackModel
import io.synapse.ai.domains.study.repository.IPackRepository
import io.synapse.ai.domains.study.repository.IQuestionRepository
import io.synapse.ai.features.add_pdf.domain.model.GenerationConfig
import io.synapse.ai.features.add_pdf.domain.model.GenerationError as DomainGenerationError
import io.synapse.ai.features.add_pdf.domain.model.GenerationStreamEvent
import io.synapse.ai.features.add_pdf.domain.model.ResolvedSource
import io.synapse.ai.features.add_pdf.domain.model.SourceRequest
import io.synapse.ai.features.add_pdf.domain.model.toPackModule
import io.synapse.ai.features.add_pdf.domain.model.toQuestionModel
import io.synapse.ai.features.add_pdf.domain.repository.IAIRepository
import io.synapse.ai.features.add_pdf.domain.resolver.SourceContentResolver
import io.synapse.ai.features.add_pdf.presentation.analytics.AddPdfAnalyticsTracker
import io.synapse.ai.features.add_pdf.presentation.state.AddPdfUiState
import io.synapse.ai.features.add_pdf.presentation.state.SourceTab
import io.synapse.ai.features.summary.domain.model.SummaryStreamEvent
import io.synapse.ai.features.summary.domain.repository.ISummaryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddPdfGenerationCoordinator @Inject constructor(
    private val aiRepo: IAIRepository,
    private val packRepo: IPackRepository,
    private val questionRepo: IQuestionRepository,
    private val authRepo: IAuthRepository,
    private val sourceResolver: SourceContentResolver,
    private val analyticsTracker: AddPdfAnalyticsTracker,
    private val summaryRepo: ISummaryRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private const val MAX_RETRIES = 3
    }

    suspend fun startGeneration(state: AddPdfUiState): Flow<AddPdfGenerationUiEvent> = flow {
        val sourceTypeStr = state.sourceTab.toAnalyticsKey()
        analyticsTracker.generationStarted(
            sourceTypeStr = sourceTypeStr,
            language = state.language,
            questionCount = state.questionCount,
            thinkingEnabled = state.thinkingEnabled && !state.isThinkingLocked
        )

        val generationStartMs = System.currentTimeMillis()
        var currentPackId = 0L

        repeat(MAX_RETRIES) { attempt ->
            val currentAttempt = attempt + 1
            try {
                emit(AddPdfGenerationUiEvent.Started(isUploading = state.sourceTab == SourceTab.FILE))
                delay(1000)

                // ── 1. Resolve source (shared between pack & summary) ────
                val request = buildSourceRequest(state)
                val resolvedResult = sourceResolver.resolve(request)
                if (resolvedResult.isFailure) {
                    emit(AddPdfGenerationUiEvent.SourceResolutionFailed(resolvedResult.exceptionOrNull()?.message))
                    return@flow
                }

                emit(AddPdfGenerationUiEvent.SourceResolved)
                val resolvedSource = resolvedResult.getOrNull()!!

                val config = GenerationConfig(
                    sourceType = resolvedSource.sourceType,
                    wantsPack = state.generatePack,
                    wantsSummary = state.generateSummary,
                    questionTypes = state.selectedTypes.toList(),
                    maxQuestions = state.questionCount,
                    difficulty = state.difficulty,
                    language = state.language,
                    thinking = state.thinkingEnabled && !state.isThinkingLocked,
                    instructions = state.focusNotes.takeIf { it.isNotBlank() },
                    pageCount = state.filePageCount,
                    summaryFocus = state.summaryFocus,
                    summaryDepth = state.summaryDepth,
                    summaryLanguage = state.summaryLanguage,
                )

                // ── 2. Pack generation (if requested) ────────────────────
                if (config.wantsPack && currentPackId == 0L) {
                    currentPackId = generatePack(
                        state = state,
                        resolvedSource = resolvedSource,
                        config = config,
                        sourceTypeStr = sourceTypeStr,
                        generationStartMs = generationStartMs,
                        currentAttempt = currentAttempt,
                    )
                }

                // ── 3. Summary generation (if requested, sequential) ─────
                if (config.wantsSummary) {
                    generateSummary(
                        state = state,
                        resolvedSource = resolvedSource,
                        config = config,
                        generationStartMs = generationStartMs,
                        currentAttempt = currentAttempt,
                    )
                }

                // ── 4. All done ──────────────────────────────────────────
                emit(AddPdfGenerationUiEvent.AllCompleted(
                    packGenerated = config.wantsPack,
                    summaryGenerated = config.wantsSummary,
                ))

                // If we reach here successfully without throwing, exit retry loop
                return@flow

            } catch (e: CancellationException) {
                throw e
            } catch (e: DomainGenerationError) {
                if (currentAttempt == MAX_RETRIES) {
                    emit(AddPdfGenerationUiEvent.GenerationError(e, isRecoverable = false, currentAttempt = currentAttempt))
                    return@flow
                }
                when (e) {
                    is DomainGenerationError.AuthenticationFailed -> authRepo.ensureSignedIn()
                    is DomainGenerationError.ServerError,
                    is DomainGenerationError.NetworkError -> { }
                    else -> {
                        emit(AddPdfGenerationUiEvent.GenerationError(e, isRecoverable = false, currentAttempt = currentAttempt))
                        return@flow
                    }
                }
            } catch (e: Exception) {
                if (currentAttempt == MAX_RETRIES) {
                    emit(AddPdfGenerationUiEvent.GenerationError(e, isRecoverable = false, currentAttempt = currentAttempt))
                    return@flow
                }
            }

            // Retry delay
            delay(1500L * currentAttempt)
        }
    }

    // ── Pack generation (extracted from original monolithic flow) ─────

    private suspend fun kotlinx.coroutines.flow.FlowCollector<AddPdfGenerationUiEvent>.generatePack(
        state: AddPdfUiState,
        resolvedSource: ResolvedSource,
        config: GenerationConfig,
        sourceTypeStr: String,
        generationStartMs: Long,
        currentAttempt: Int,
    ): Long {
        var currentPackId = 0L

        aiRepo.generatePackStream(source = resolvedSource.content, generationConfig = config)
            .collect { event ->
                when (event) {
                    is GenerationStreamEvent.PackMeta -> {
                        val pack = PackModel(
                            title = event.title.ifBlank { state.packTitle },
                            sourceType = resolvedSource.sourceType,
                            createdAt = System.currentTimeMillis(),
                            note = event.description ?: "",
                            sourceSummary = event.sourceSummary,
                            category = event.category,
                            emoji = event.emoji,
                            color = event.color,
                            language = event.language?.ifBlank { state.language } ?: state.language,
                            uuid = event.packId,
                            difficulty = event.difficulty,
                            questionCount = event.expectedCount,
                            tags = event.tags,
                            estimatedMinutes = event.estimatedMinutes,
                        )
                        val localPackId = withContext(ioDispatcher) { packRepo.createPack(pack) }
                        currentPackId = localPackId

                        emit(
                            AddPdfGenerationUiEvent.PackMetaCreated(
                                localPackId = localPackId,
                                packUuid = event.packId,
                                title = pack.title,
                                emoji = pack.emoji ?: "",
                                color = pack.color ?: "",
                                conceptsFound = event.conceptsFound,
                                questionsExpected = event.expectedCount
                            )
                        )
                    }
                    is GenerationStreamEvent.Curriculum -> {
                        if (currentPackId > 0 && event.modules.isNotEmpty()) {
                            val packModules = event.modules.map { it.toPackModule() }
                            val modulesJson = io.synapse.ai.domains.study.data.mapper.serializeModules(packModules)
                            if (modulesJson != null) {
                                withContext(ioDispatcher) { packRepo.updateModules(currentPackId, modulesJson) }
                            }
                        }
                        emit(AddPdfGenerationUiEvent.CurriculumOrganized(event.modules.size))
                    }
                    is GenerationStreamEvent.Question -> {
                        if (currentPackId > 0) {
                            val model = event.toQuestionModel(currentPackId)
                            withContext(ioDispatcher) { questionRepo.insertQuestions(listOf(model)) }
                        }
                        emit(AddPdfGenerationUiEvent.QuestionAdded)
                    }
                    is GenerationStreamEvent.Progress -> {
                        emit(AddPdfGenerationUiEvent.ProgressUpdated(event.percent / 100f, event.message, event.conceptsFound))
                    }
                    is GenerationStreamEvent.Done -> {
                        val durationMs = System.currentTimeMillis() - generationStartMs
                        analyticsTracker.generationSucceeded(
                            sourceTypeStr = sourceTypeStr,
                            language = state.language,
                            questionCount = event.total,
                            durationMs = durationMs
                        )

                        if (currentPackId > 0) {
                            withContext(ioDispatcher) {
                                packRepo.updateQuestionCount(currentPackId, event.total)
                            }
                        }

                        val uiQuestions = withContext(ioDispatcher) {
                            questionRepo.observeQuestionsForPack(currentPackId).first().map { it.toUiModel() }
                        }

                        emit(AddPdfGenerationUiEvent.PackCompleted(event.total, durationMs, sourceTypeStr, uiQuestions))
                    }
                    is GenerationStreamEvent.Error -> {
                        emit(
                            AddPdfGenerationUiEvent.GenerationError(
                                error = Exception(event.message),
                                isRecoverable = event.recoverable,
                                currentAttempt = currentAttempt
                            )
                        )
                        if (!event.recoverable) return@collect
                    }
                }
            }

        return currentPackId
    }

    // ── Summary generation ───────────────────────────────────────────

    private suspend fun kotlinx.coroutines.flow.FlowCollector<AddPdfGenerationUiEvent>.generateSummary(
        state: AddPdfUiState,
        resolvedSource: ResolvedSource,
        config: GenerationConfig,
        generationStartMs: Long,
        currentAttempt: Int,
    ) {
        emit(AddPdfGenerationUiEvent.SummaryPhaseStarted)

        val summaryConfig = io.synapse.ai.features.summary.domain.model.SummaryConfig(
            sourceType = resolvedSource.sourceType,
            sourceContent = resolvedSource.content,
            language = config.summaryLanguage.ifBlank { config.language },
            instructions = config.instructions,
            pageCount = config.pageCount,
        )

        summaryRepo.generateSummaryStream(summaryConfig).collect { event ->
            when (event) {
                is SummaryStreamEvent.SummaryMetadata -> {
                    emit(
                        AddPdfGenerationUiEvent.SummaryMetaCreated(
                            localSummaryId = 0L, // ID is set by repository persistence
                            title = event.title,
                            emoji = event.emoji,
                            color = event.color,
                            conceptsFound = event.conceptsFound,
                            sectionsExpected = event.expectedSections,
                        )
                    )
                }
                is SummaryStreamEvent.SectionGenerated -> {
                    emit(AddPdfGenerationUiEvent.SummarySectionAdded)
                }
                is SummaryStreamEvent.Shared -> {
                    when (val inner = event.event) {
                        is GenerationStreamEvent.Progress -> {
                            emit(
                                AddPdfGenerationUiEvent.SummaryProgressUpdated(
                                    percent = inner.percent / 100f,
                                    message = inner.message,
                                    conceptsFound = inner.conceptsFound,
                                )
                            )
                        }
                        is GenerationStreamEvent.Done -> {
                            val durationMs = System.currentTimeMillis() - generationStartMs
                            // The summary repo patches packId with localSummaryId
                            val localSummaryId = inner.packId?.toLongOrNull() ?: 0L

                            // Emit an updated SummaryMetaCreated with the real local ID
                            if (localSummaryId > 0L) {
                                emit(
                                    AddPdfGenerationUiEvent.SummaryMetaCreated(
                                        localSummaryId = localSummaryId,
                                        title = "",   // empty = no title update
                                        emoji = "",
                                        color = "",
                                        conceptsFound = 0,
                                        sectionsExpected = 0,
                                    )
                                )
                            }

                            emit(
                                AddPdfGenerationUiEvent.SummaryCompleted(
                                    totalSections = inner.total,
                                    durationMs = durationMs,
                                )
                            )
                        }
                        is GenerationStreamEvent.Error -> {
                            emit(
                                AddPdfGenerationUiEvent.GenerationError(
                                    error = Exception(inner.message),
                                    isRecoverable = inner.recoverable,
                                    currentAttempt = currentAttempt,
                                )
                            )
                        }
                        else -> { /* other shared events */ }
                    }
                }
            }
        }
    }

    private fun buildSourceRequest(state: AddPdfUiState): SourceRequest {
        return when (state.sourceTab) {
            SourceTab.FILE -> SourceRequest(
                type = SourceRequest.RequestType.FILE,
                uri = state.fileUri,
                fileName = state.fileName
            )
            SourceTab.TEXT -> SourceRequest(
                type = SourceRequest.RequestType.TEXT,
                text = state.pasteText
            )
            SourceTab.WEB -> SourceRequest(
                type = SourceRequest.RequestType.WEB,
                url = state.webUrl
            )
            SourceTab.YOUTUBE -> SourceRequest(
                type = SourceRequest.RequestType.YOUTUBE,
                url = state.webUrl
            )
        }
    }

    private fun SourceTab.toAnalyticsKey(): String = when (this) {
        SourceTab.FILE    -> "pdf"
        SourceTab.TEXT    -> "text"
        SourceTab.WEB     -> "url"
        SourceTab.YOUTUBE -> "youtube"
    }
}
