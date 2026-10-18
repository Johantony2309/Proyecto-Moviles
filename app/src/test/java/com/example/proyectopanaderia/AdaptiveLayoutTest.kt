package com.example.proyectopanaderia

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.proyectopanaderia.presentation.components.adaptiveColumnCount
import com.example.proyectopanaderia.presentation.components.hasCompactHeight
import com.example.proyectopanaderia.presentation.components.hasWideLayout
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class AdaptiveLayoutTest {
    private fun window(width: Int, height: Int = 800) =
        WindowSizeClass.calculateFromSize(DpSize(width.dp, height.dp))

    @Test fun widthBreakpointsSelectBottomOrSideNavigation() {
        assertEquals(WindowWidthSizeClass.Compact, window(599).widthSizeClass)
        assertFalse(window(599).hasWideLayout)
        assertEquals(WindowWidthSizeClass.Medium, window(600).widthSizeClass)
        assertEquals(WindowWidthSizeClass.Medium, window(839).widthSizeClass)
        assertEquals(WindowWidthSizeClass.Expanded, window(840).widthSizeClass)
        assertTrue(window(600).hasWideLayout)
        assertTrue(window(840).hasWideLayout)
    }

    @Test fun horizontalWindowUsesCompactHeightWithoutLosingWideLayout() {
        assertTrue(window(800, 479).hasCompactHeight)
        assertTrue(window(800, 479).hasWideLayout)
        assertFalse(window(800, 480).hasCompactHeight)
    }

    @Test fun contentColumnsAccountForRailAndAvailableSpace() {
        assertEquals(1, adaptiveColumnCount(window(599).widthSizeClass, 559.dp, 1f))
        assertEquals(2, adaptiveColumnCount(window(600).widthSizeClass, 520.dp, 1f))
        assertEquals(3, adaptiveColumnCount(window(840).widthSizeClass, 760.dp, 1f))
        assertEquals(1, adaptiveColumnCount(window(600).widthSizeClass, 450.dp, 1f))
    }

    @Test fun largeTextReducesColumnsAndNarrowPanesKeepOneColumn() {
        val expanded = window(840).widthSizeClass
        assertEquals(2, adaptiveColumnCount(expanded, 760.dp, 1.5f))
        assertEquals(1, adaptiveColumnCount(expanded, 760.dp, 2f))
        assertEquals(1, adaptiveColumnCount(expanded, 100.dp, 2f))
    }

    @Test fun ticketIsLimitedToTwoPanelsEvenOnLargeWindows() {
        assertEquals(2, adaptiveColumnCount(window(1600).widthSizeClass, 1520.dp, 1f, maxColumns = 2))
    }
}
