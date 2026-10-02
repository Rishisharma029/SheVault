package com.shevault.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shevault.core.design.theme.SheVaultTheme

/**
 * Top App Bar with discreet decoy quick-toggle and status indication.
 */
@Composable
fun SheVaultTopBar(
    title: String,
    modifier: Modifier = Modifier,
    isDiscreteMode: Boolean = false,
    onToggleDiscreteMode: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SheVaultTheme.colors.surface)
            .padding(horizontal = SheVaultTheme.spacing.screenHorizontalPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationIcon != null) {
                navigationIcon()
                Spacer(modifier = Modifier.width(SheVaultTheme.spacing.sm))
            } else {
                Icon(
                    imageVector = if (isDiscreteMode) Icons.Default.Calculate else Icons.Default.Shield,
                    contentDescription = if (isDiscreteMode) "Calculator" else "SheVault",
                    tint = SheVaultTheme.colors.primary,
                    modifier = Modifier.padding(end = SheVaultTheme.spacing.sm)
                )
            }

            Text(
                text = if (isDiscreteMode) "Calculator" else title,
                style = SheVaultTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SheVaultTheme.colors.onSurface
                ),
                modifier = Modifier.weight(1f)
            )

            if (onToggleDiscreteMode != null) {
                IconButton(
                    onClick = onToggleDiscreteMode
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VisibilityOff,
                        contentDescription = "Toggle Decoy Mode",
                        tint = SheVaultTheme.colors.onSurfaceVariant
                    )
                }
            }

            if (actions != null) {
                actions()
            }
        }
    }
}
