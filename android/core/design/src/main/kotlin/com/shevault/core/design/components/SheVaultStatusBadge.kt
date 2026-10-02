package com.shevault.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.shevault.core.design.theme.SheVaultTheme
import com.shevault.core.design.tokens.SafetyState

/**
 * Semantic Safety Status Badge displaying current protection or alert level.
 */
@Composable
fun SheVaultStatusBadge(
    state: SafetyState,
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    val style = SheVaultTheme.status.forState(state)

    Box(
        modifier = modifier
            .clip(SheVaultTheme.radius.shapePill)
            .background(style.background)
            .border(
                width = SheVaultTheme.iconRules.hairlineStroke,
                color = style.border,
                shape = SheVaultTheme.radius.shapePill
            )
            .padding(
                horizontal = SheVaultTheme.spacing.md,
                vertical = SheVaultTheme.spacing.xs
            )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // State indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(style.content)
            )
            Spacer(modifier = Modifier.width(SheVaultTheme.spacing.sm))
            Text(
                text = customText ?: style.labelText,
                style = SheVaultTheme.typography.labelMedium.copy(
                    color = style.content
                )
            )
        }
    }
}
