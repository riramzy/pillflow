package com.riramzy.pillfllow.ui.components.custom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.notifications
import pillfllow.shared.generated.resources.pillflow_logo
import pillfllow.shared.generated.resources.profile

@Composable
fun PillFlowTopAppBar(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.pillflow_logo),
                    modifier = Modifier.size(26.dp),
                    contentDescription = "null"
                )

                Text(
                    text = "PillFlow",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .wrapContentSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(0.3f),
                            shape = CircleShape
                        )
                        .clickable { onNotificationsClick() },
                ) {
                    Image(
                        imageVector = vectorResource(Res.drawable.notifications),
                        modifier = Modifier.size(18.dp),
                        contentScale = ContentScale.FillBounds,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        contentDescription = "null"
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(0.3f),
                            shape = CircleShape
                        )
                        .clickable { onProfileClick() },
                ) {
                    Image(
                        painter = painterResource(Res.drawable.profile),
                        modifier = Modifier.size(18.dp),
                        contentScale = ContentScale.FillBounds,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        contentDescription = "null"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PillFlowTopAppBarPreview() {
    PillFlowTheme {
        PillFlowTopAppBar(modifier = Modifier.padding(15.dp))
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PillFlowTopAppBarPreviewDark() {
    PillFlowTheme {
        PillFlowTopAppBar(modifier = Modifier.padding(15.dp))
    }
}

