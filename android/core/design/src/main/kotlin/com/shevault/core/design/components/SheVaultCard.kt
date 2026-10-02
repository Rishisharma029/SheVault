package com.shevault.core.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.shevault.core.design.theme.SheVaultTheme

/**
 * Standard Surface Card for SheVault dashboards and widgets.
 */
@Composable
fun SheVaultCard(
    modifier: Modifier = Modifier,
    shape: Shape = SheVaultTheme.radius.shapeLg,
    containerColor: Color = SheVaultTheme.colors.surface,
    elevation: Dp = SheVaultTheme.elevation.level1,
    border: BorderStroke? = BorderStroke(
        width = SheVaultTheme.iconRules.hairlineStroke,
        color = SheVaultTheme.colors.outlineVariant
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        ),
        border = border
    ) {
        Column(
            modifier = Modifier.padding(SheVaultTheme.spacing.cardContentPadding),
            content = content
        )
    }
}
