package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScrimMap
import com.example.model.ScrimMode
import com.example.model.ScrimStatus
import com.example.ui.theme.*

@Composable
fun StatusBadge(status: ScrimStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        ScrimStatus.LIVE -> CrimsonPrimary to TextPrimary
        ScrimStatus.UPCOMING -> ElectricBlue.copy(alpha = 0.2f) to ElectricBlue
        ScrimStatus.FULL -> WarningOrange.copy(alpha = 0.2f) to WarningOrange
        ScrimStatus.COMPLETED -> TextSecondary.copy(alpha = 0.2f) to TextSecondary
        ScrimStatus.CANCELLED -> ErrorRed.copy(alpha = 0.2f) to ErrorRed
        ScrimStatus.DRAFT -> TextMuted.copy(alpha = 0.2f) to TextMuted
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("badge_status_${status.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (status == ScrimStatus.LIVE) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = status.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun ModeMapChip(mode: ScrimMode, map: ScrimMap, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BrandCardElevated)
            .border(1.dp, BrandBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = mode.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = GoldAccent
        )
        Text(
            text = " • ",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Text(
            text = map.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
fun PrizeTag(amountNpr: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(GoldAccent.copy(alpha = 0.15f), CrimsonPrimary.copy(alpha = 0.1f))
                )
            )
            .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "रू",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = GoldAccent,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "%,d".format(amountNpr),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null, actionText: String? = null, onActionClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CrimsonPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                )
            }
        }

        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = CrimsonPrimary
                )
            }
        }
    }
}
