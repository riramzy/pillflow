package com.riramzy.pillfllow.ui.components.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riramzy.pillfllow.ui.theme.PillFlowTheme
import org.jetbrains.compose.resources.painterResource
import pillfllow.shared.generated.resources.Res
import pillfllow.shared.generated.resources.apple
import pillfllow.shared.generated.resources.google

@Composable
fun PillFlowSignWithButton(
    modifier: Modifier = Modifier,
    isWithGoogle: Boolean = true,
    isSignUp: Boolean = false,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit = {}
) {
    val (bgnColor, icon) = when(isWithGoogle) {
        true -> Pair(
            Color(0xFF4285F4),
            Res.drawable.google
        )
        false -> Pair(
            if (isSystemInDarkTheme()) Color.White else Color.Black,
            Res.drawable.apple
        )
    }

    val btnText = when(isSignUp) {
        true -> if (isWithGoogle) "Sign Up with Google" else "Sign Up with Apple"
        false -> if (isWithGoogle) "Sign In with Google" else "Sign In with Apple"
    }

    val appleColorFilter = if (icon == Res.drawable.apple) {
        if (isSystemInDarkTheme()) ColorFilter.tint(Color.Black) else ColorFilter.tint(Color.White)
    } else null

    val txtColor = if (icon == Res.drawable.google) {
        Color.White
    } else if (isSystemInDarkTheme()) Color.Black else Color.White

    Button(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgnColor,
            disabledContainerColor = bgnColor.copy(alpha = 0.6f)
        ),
        enabled = isEnabled,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(vertical = 6.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = txtColor
                )
            } else if (isWithGoogle) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        )
                ) {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = "Sign Icon",
                        modifier = Modifier
                            .padding(2.dp)
                    )
                }
            } else {
                Image(
                    painter = painterResource(icon),
                    contentDescription = "Sign Icon",
                    modifier = Modifier
                        .size(24.dp),
                    colorFilter = appleColorFilter
                )
            }

            Text(
                text = if (isLoading) "Connecting..." else btnText,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = txtColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PillFlowSignWithButtonPreview() {
    PillFlowTheme {
        PillFlowSignWithButton(modifier = Modifier.padding(15.dp))
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PillFlowSignWithButtonPreviewDark() {
    PillFlowTheme {
        PillFlowSignWithButton(modifier = Modifier.padding(15.dp))
    }
}