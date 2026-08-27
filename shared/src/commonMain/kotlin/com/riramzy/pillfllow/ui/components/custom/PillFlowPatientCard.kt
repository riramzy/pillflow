package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.riramzy.pillfllow.ui.components.dashboard.caregiver.SelectedPatientCard
import com.riramzy.pillfllow.ui.state.dashboard.PairedPatientUiModel
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import org.jetbrains.compose.resources.painterResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.unlink

@Composable
fun PillFlowPatientCard(
    patient: PairedPatientUiModel = PairedPatientUiModel(),
    onPatientClick: (String) -> Unit = {},
    onUnlinkClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(50.dp),
        onClick = { onPatientClick(patient.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SelectedPatientCard(
                name = patient.name,
                avatar = patient.avatar,
                relation = patient.relation,
                status = patient.status,
                lateDosesCount = patient.lateDosesCount,
                missedDosesCount = patient.missedDosesCount,
                compliancePercentage = patient.compliancePercentage
            )

            IconButton(
                onClick = { onUnlinkClick() },
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(Res.drawable.unlink),
                    contentDescription = "Unlink",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(35.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowPatientCardPreview() {
    PillFlowTheme {
        PillFlowPatientCard(modifier = Modifier.padding(15.dp))
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowPatientCardPreviewDark() {
    PillFlowTheme {
        PillFlowPatientCard(modifier = Modifier.padding(15.dp))
    }
}