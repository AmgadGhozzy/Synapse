package io.synapse.ai.features.add_pdf.presentation.coordinator

import io.synapse.ai.core.ui.model.toUiModel
import io.synapse.ai.domains.auth.repository.IAuthRepository
import io.synapse.ai.domains.study.model.PackModel
import io.synapse.ai.domains.study.repository.IPackRepository
import io.synapse.ai.domains.study.repository.IQuestionRepository
import io.synapse.ai.features.add_pdf.domain.model.GenerationConfig
import io.synapse.ai.features.add_pdf.domain.model.GenerationError as DomainGenerationError
import io.synapse.ai.features.add_pdf.domain.model.GenerationStreamEvent
import io.synapse.ai.features.add_pdf.domain.model.SourceRequest
import io.synapse.ai.features.add_pdf.domain.model.toPackModule
import io.synapse.ai.features.add_pdf.domain.model.toQuestionModel
import io.synapse.ai.features.add_pdf.domain.repository.IAIRepository
import io.synapse.ai.features.add_pdf.domain.resolver.SourceContentResolver
import io.synapse.ai.features.add_pdf.presentation.analytics.AddPdfAnalyticsTracker
import io.synapse.ai.features.add_pdf.presentation.state.AddPdfUiState
import io.synapse.ai.features.add_pdf.presentation.state.SourceTab
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
                    questionTypes = state.selectedTypes.toList(),
                    maxQuestions = state.questionCount,
                    difficulty = state.difficulty,
                    language = state.language,
                    thinking = state.thinkingEnabled && !state.isThinkingLocked,
                    instructions = state.focusNotes.takeIf { it.isNotBlank() },
                    pageCount = state.filePageCount,
                )

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

                                emit(AddPdfGenerationUiEvent.Completed(event.total, durationMs, sourceTypeStr, uiQuestions))
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

                // If we reach here successfully without throwing, exit retry loop
                return@flow

            } catch (e: CancellationException) {
                // Let the UI track questionsCompleted, so we can't emit it here easily,
                // but the ViewModel can call tracking on cancel
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
