package com.venom.synapse.features.stats.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.venom.synapse.R
import com.venom.synapse.core.theme.tokens.Radius
import com.venom.synapse.core.theme.tokens.Spacing
import com.venom.ui.components.common.adp
import com.venom.ui.components.common.asp


@Composable
fun AiInsightCard(
    accuracyDelta: Float,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val accentColor = MaterialTheme.colorScheme.secondary

    Surface(
        onClick  = { expanded = !expanded },
        modifier = modifier.fillMaxWidth(),
        shape    = Radius.ShapeXL,
        color    = accentColor.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.CardInternalPadding)
                .animateContentSize(),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing10),
            ) {
                Box(
                    modifier         = Modifier
                        .size(36.adp)
                        .background(accentColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_sparkles),
                        contentDescription = stringResource(R.string.ai_insight_icon_description),
                        tint               = Color.White,
                        modifier           = Modifier.size(18.adp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = stringResource(R.string.ai_insight_title),
                        fontSize   = 11.asp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = accentColor,
                    )
                    Text(
                        text       = stringResource(R.string.ai_insight_headline),
                        fontSize   = 13.asp,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Icon(
                    painter            = painterResource(
                        if (expanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down,
                    ),
                    contentDescription = stringResource(
                        if (expanded) R.string.collapse else R.string.expand,
                    ),
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(18.adp),
                )
            }

            AnimatedVisibility(visible = expanded) {
                Text(
                    text       = stringResource(R.string.ai_insight_body, accuracyDelta),
                    fontSize   = 12.asp,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.asp,
                    modifier   = Modifier.padding(top = Spacing.ListItemVerticalGap),
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AiInsightCardPreview() {
    MaterialTheme {
        AiInsightCard(
            accuracyDelta = 15f,
            modifier      = Modifier.padding(Spacing.ScreenHorizontalPadding),
        )
    }
}