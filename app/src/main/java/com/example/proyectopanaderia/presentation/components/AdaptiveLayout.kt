package com.example.proyectopanaderia.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Recalculated on resize, rotation and density changes, including multi-window. */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun currentWindowSizeClass(): WindowSizeClass {
    val size = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    return remember(size, density) {
        with(density) { WindowSizeClass.calculateFromSize(DpSize(size.width.toDp(), size.height.toDp())) }
    }
}

internal val WindowSizeClass.hasWideLayout: Boolean
    get() = widthSizeClass != WindowWidthSizeClass.Compact

internal val WindowSizeClass.hasCompactHeight: Boolean
    get() = heightSizeClass == WindowHeightSizeClass.Compact

internal fun adaptiveColumnCount(
    widthClass: WindowWidthSizeClass, availableWidth: Dp, fontScale: Float, maxColumns: Int = 3,
): Int {
    val classLimit = when (widthClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        else -> 3
    }
    // Account for grid padding, gaps, navigation rail and large system text.
    val fittingColumns = ((availableWidth.value - 40f + 14f) / (220f * fontScale.coerceAtLeast(1f) + 14f)).toInt()
    return fittingColumns.coerceIn(1, minOf(classLimit, maxColumns))
}

@Composable
internal fun AdaptiveScreenGrid(
    modifier: Modifier = Modifier, maxColumns: Int = 3, content: LazyGridScope.() -> Unit,
) {
    val sizeClass = currentWindowSizeClass()
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(modifier) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(adaptiveColumnCount(sizeClass.widthSizeClass, maxWidth, fontScale, maxColumns)),
            modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}
