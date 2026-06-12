package com.rentsplit.ui.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import com.rentsplit.ui.main.LocalBottomBarPadding
import com.rentsplit.ui.main.LocalTopBarPadding
import com.rentsplit.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onMonthClick: (Int, Int) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.historyUiState.collectAsStateWithLifecycle()
    val uiError by viewModel.uiError.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiError) {
        uiError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = LocalAppColors.current.surface0,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        AnimatedContent(
            targetState = uiState.isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "HistoryScreenContent"
        ) { isLoading ->
            if (isLoading) {
                HistoryShimmer()
            } else if (uiState.monthlySummaries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().background(LocalAppColors.current.surface0),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", style = MaterialTheme.typography.displaySmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No history yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = LocalAppColors.current.textSecondary
                        )
                        Text(
                            "Add expenses on the Home tab",
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalAppColors.current.textMuted
                        )
                    }
                }
            } else {
                val groupedByYear = uiState.monthlySummaries.groupBy { it.year }
                val topPadding = LocalTopBarPadding.current
                var isRefreshing by remember { mutableStateOf(false) }
                val haptic = LocalHapticFeedback.current
                val scope = rememberCoroutineScope()

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        scope.launch {
                            isRefreshing = true
                            try {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } catch (e: Exception) {}
                            kotlinx.coroutines.delay(800)
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LocalAppColors.current.surface0),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = topPadding + 8.dp,
                            bottom = LocalBottomBarPadding.current + 80.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        groupedByYear.forEach { (year, summaries) ->
                            item {
                                // Year group header
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(18.dp)
                                            .background(CyanPrimary, RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = year.toString(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = CyanPrimary
                                    )
                                }
                            }
                            itemsIndexed(
                                items = summaries,
                                key = { _, summary -> "${summary.month}_${summary.year}" }
                            ) { index, summary ->
                                var visible by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) { visible = true }
                                AnimatedVisibility(
                                    visible = visible,
                                    modifier = Modifier.animateItem(),
                                    enter = slideInVertically(
                                        initialOffsetY = { 40 },
                                        animationSpec = tween(250, delayMillis = index * 50)
                                    ) + fadeIn(animationSpec = tween(250, delayMillis = index * 50))
                                ) {
                                    HistoryMonthCard(
                                        summary = summary,
                                        onClick = { onMonthClick(summary.month, summary.year) }
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryMonthCard(summary: MonthlySummary, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val monthEmoji = when (summary.month) {
        1 -> "❄️"; 2 -> "🌸"; 3 -> "🌿"; 4 -> "🌦️"
        5 -> "☀️"; 6 -> "🌊"; 7 -> "🏖️"; 8 -> "🌻"
        9 -> "🍂"; 10 -> "🎃"; 11 -> "🍁"; else -> "🎄"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface1)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Month emoji badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(colors.surface2, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = monthEmoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = summary.monthName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = summary.year.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RM ${"%.2f".format(summary.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CyanPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
