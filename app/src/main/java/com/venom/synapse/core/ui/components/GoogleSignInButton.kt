package com.venom.synapse.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.venom.resources.R
import com.venom.synapse.core.theme.synapse
import com.venom.ui.components.common.adp
import com.venom.synapse.R as SynapseR

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(60.adp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1F1F1F)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.adp,
            pressedElevation = 0.adp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = MaterialTheme.synapse.spacing.s8)
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_google),
                contentDescription = stringResource(SynapseR.string.google_logo_description),
                modifier = Modifier.size(24.adp)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.synapse.spacing.s12))
            Text(
                text = stringResource(SynapseR.string.google_sign_in_button),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
