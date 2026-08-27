package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import org.jetbrains.compose.resources.painterResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.compliance_missed
import pillfllow.shared.generated.resources.compliance_ontime

@Composable
fun PillFlowSnackbar(
    modifier: Modifier = Modifier,
    snackbarData: SnackbarData? = null,
    isError: Boolean = false
) {
    Card(
        modifier = modifier
            .wrapContentSize()
            .padding(15.dp),
        shape = CircleShape,
        colors = cardColors(
            containerColor = if (isError) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(3f)
            ) {
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .background(
                            color = if (isError) {
                                MaterialTheme.colorScheme.error.copy(0.3f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(0.3f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (isError) Res.drawable.compliance_missed else Res.drawable.compliance_ontime),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = snackbarData?.visuals?.message ?: "Message",
                    modifier = Modifier.padding(start = 15.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            snackbarData?.visuals?.actionLabel?.let { action ->
                PillFlowButton(
                    text = action,
                    onClick = { snackbarData.performAction() },
                    customColor = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    customTextColor = if (isError) {
                        MaterialTheme.colorScheme.onError
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowSnackbarPreview() {
    PillFlowTheme {
        val snackbarData = object : SnackbarData {
            override val visuals = object : SnackbarVisuals {
                override val message: String
                    get() = "Error Message"
                override val actionLabel: String = "Dismiss"
                override val duration: SnackbarDuration = SnackbarDuration.Short
                override val withDismissAction: Boolean = true
            }

            override fun performAction() {
                TODO("Not yet implemented")
            }

            override fun dismiss() {
                TODO("Not yet implemented")
            }
        }

        PillFlowSnackbar(
            snackbarData = snackbarData,
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowSnackbarDarkPreview() {
    PillFlowTheme {
        val snackbarData = object : SnackbarData {
            override val visuals = object : SnackbarVisuals {
                override val message: String
                    get() = "Error Message"
                override val actionLabel: String? = null
                override val duration: SnackbarDuration = SnackbarDuration.Short
                override val withDismissAction: Boolean = false
            }

            override fun performAction() {
                TODO("Not yet implemented")
            }

            override fun dismiss() {
                TODO("Not yet implemented")
            }
        }

        PillFlowSnackbar(
            snackbarData = snackbarData,
        )
    }
}