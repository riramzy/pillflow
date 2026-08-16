package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.history
import pillfllow.shared.generated.resources.home
import pillfllow.shared.generated.resources.prescriptions
import pillfllow.shared.generated.resources.settings

@Composable
fun PillFlowBottomNavBar(
    modifier: Modifier = Modifier,
    selectedPage: String = "Home",
    onHomeClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onPrescriptionsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .wrapContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(50.dp)
    ) {
        when (selectedPage) {
            "Home" -> {
                Row(
                    modifier = Modifier
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PillFlowNavItemExpanded(icon = Res.drawable.home, name = "Home")
                    PillFlowNavItem(icon = Res.drawable.history, onNavItemClick = { onHistoryClick() })
                    PillFlowNavItem(icon = Res.drawable.prescriptions, onNavItemClick = { onPrescriptionsClick() })
                    PillFlowNavItem(icon = Res.drawable.settings, onNavItemClick = { onSettingsClick() })
                }
            }
            "History" -> {
                Row(
                    modifier = Modifier
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PillFlowNavItem(icon = Res.drawable.home, onNavItemClick = { onHomeClick() })
                    PillFlowNavItemExpanded(icon = Res.drawable.history, name = "History")
                    PillFlowNavItem(icon = Res.drawable.prescriptions, onNavItemClick = { onPrescriptionsClick() })
                    PillFlowNavItem(icon = Res.drawable.settings, onNavItemClick = { onSettingsClick() })
                }
            }
            "Caregiver" -> {
                Row(
                    modifier = Modifier
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PillFlowNavItem(icon = Res.drawable.home, onNavItemClick = { onHomeClick() })
                    PillFlowNavItem(icon = Res.drawable.history, onNavItemClick = { onHistoryClick() })
                    PillFlowNavItemExpanded(icon = Res.drawable.prescriptions, name = "Prescriptions")
                    PillFlowNavItem(icon = Res.drawable.settings, onNavItemClick = { onSettingsClick() })
                }
            }
            "Settings" -> {
                Row(
                    modifier = Modifier
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PillFlowNavItem(icon = Res.drawable.home, onNavItemClick = { onHomeClick() })
                    PillFlowNavItem(icon = Res.drawable.history, onNavItemClick = { onHistoryClick() })
                    PillFlowNavItem(icon = Res.drawable.prescriptions, onNavItemClick = { onPrescriptionsClick() })
                    PillFlowNavItemExpanded(icon = Res.drawable.settings, name = "Settings")
                }
            }
            else -> {
                Row(
                    modifier = Modifier
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PillFlowNavItem(icon = Res.drawable.home, onNavItemClick = { onHomeClick() })
                    PillFlowNavItem(icon = Res.drawable.history, onNavItemClick = { onHistoryClick() })
                    PillFlowNavItem(icon = Res.drawable.prescriptions, onNavItemClick = { onPrescriptionsClick() })
                    PillFlowNavItem(icon = Res.drawable.settings, onNavItemClick = { onSettingsClick() })
                }
            }
        }
    }
}

@Composable
fun PillFlowNavItem(icon: DrawableResource, onNavItemClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(0.3f),
                shape = CircleShape
            )
            .clickable { onNavItemClick() }
    ) {
        Icon(
            imageVector = vectorResource(icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
fun PillFlowNavItemExpanded(icon: DrawableResource, name: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50.dp)
            )
            .height(36.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = vectorResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )

            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview
@Composable
fun PillFlowBottomNavBarPreview() {
    PillFlowTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PillFlowBottomNavBar(selectedPage = "Home")
            PillFlowBottomNavBar(selectedPage = "History")
            PillFlowBottomNavBar(selectedPage = "Caregiver")
            PillFlowBottomNavBar(selectedPage = "Settings")
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PillFlowBottomNavBarPreviewDark() {
    PillFlowTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PillFlowBottomNavBar(selectedPage = "Home")
            PillFlowBottomNavBar(selectedPage = "History")
            PillFlowBottomNavBar(selectedPage = "Caregiver")
            PillFlowBottomNavBar(selectedPage = "Settings")
        }
    }
}