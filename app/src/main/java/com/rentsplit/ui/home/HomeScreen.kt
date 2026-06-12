package com.rentsplit.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rentsplit.ui.components.MemberAvatar
import com.rentsplit.ui.components.bounceClick
import com.rentsplit.ui.main.LocalBottomBarPadding
import com.rentsplit.ui.main.LocalTopBarPadding
import com.rentsplit.ui.theme.*
import com.rentsplit.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    showAddExpense: Boolean = false,
    onShowAddExpenseChange: (Boolean) -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val colors = LocalAppColors.current

    LaunchedEffect(uiState.uiError) {
        uiState.uiError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = colors.surface0,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        AnimatedContent(
            targetState = uiState.isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "HomeScreenContent"
        ) { isLoading ->
            if (isLoading) {
                HomeShimmer()
            } else {
                val bottomPadding = LocalBottomBarPadding.current
                val topPadding = LocalTopBarPadding.current
                var isRefreshing by remember { mutableStateOf(false) }
                val haptic = LocalHapticFeedback.current

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
                            .background(colors.surface0),
                        contentPadding = PaddingValues(
                            top = topPadding + 8.dp,
                            bottom = bottomPadding + 88.dp
                        )
                    ) {
                        // ── Hero Card ─────────────────────────────────────────────────
                        item {
                            HeroCard(
                                uiState = uiState,
                                onPreviousMonth = viewModel::previousMonth,
                                onNextMonth = viewModel::nextMonth
                            )
                        }

                        // ── Section: Budget Progress ──────────────────────────────────
                        if (uiState.categoryBudgets.any { it.category.budgetLimit != null && it.category.budgetLimit > 0.0 }) {
                            item {
                                BudgetProgressCard(categoryBudgets = uiState.categoryBudgets)
                            }
                        }

                        // ── Section: Spending Trends ──────────────────────────────────
                        if (uiState.spendingHistory.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Spending Trends", modifier = Modifier.padding(horizontal = 20.dp))
                            }
                            item {
                                SpendingChart(history = uiState.spendingHistory)
                            }
                        }

                        // ── Section: Housemates ───────────────────────────────────────
                        if (uiState.memberStats.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Housemates", modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                            }
                            itemsIndexed(uiState.memberStats, key = { _, stat -> stat.member.id }) { index, stat ->
                                var visible by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) { visible = true }
                                AnimatedVisibility(
                                    visible = visible,
                                    modifier = Modifier.animateItem(),
                                    enter = slideInVertically(
                                        initialOffsetY = { 60 },
                                        animationSpec = tween(durationMillis = 300, delayMillis = index * 60)
                                    ) + fadeIn(animationSpec = tween(300, delayMillis = index * 60))
                                ) {
                                    MemberCard(
                                        stat = stat,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // ── Empty State ───────────────────────────────────────────────
                        if (uiState.totalExpenses == 0.0) {
                            item { EmptyState() }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }

        if (showAddExpense) {
            AddExpenseBottomSheet(
                onDismiss = { onShowAddExpenseChange(false) },
                onSuccess = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Expense added ✓")
                    }
                }
            )
        }
    }
}

// ── Hero Card ─────────────────────────────────────────────────────────────────

@Composable
fun HeroCard(
    uiState: HomeUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = colors.gradientHero
                )
            )
    ) {
        // Subtle glow accent top-right
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 120.dp, y = (-60).dp)
                .background(CyanPrimary.copy(alpha = 0.07f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = (-40).dp, y = 80.dp)
                .background(PurpleAccent.copy(alpha = 0.06f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Month selector row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier
                        .size(36.dp)
                        .background(colors.surface3.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = uiState.selectedDate.format(formatter),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textSecondary
                )
                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier
                        .size(36.dp)
                        .background(colors.surface3.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Total Expenses",
                style = MaterialTheme.typography.labelLarge,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "RM ${"%.2f".format(uiState.totalExpenses)}",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Quick stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                HeroStat(
                    label = "Housemates",
                    value = uiState.memberStats.size.toString(),
                    color = CyanPrimary
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(colors.surface4)
                )
                HeroStat(
                    label = "Split",
                    value = if (uiState.memberStats.size > 1)
                        "RM ${"%.2f".format(uiState.totalExpenses / uiState.memberStats.size)}" else "—",
                    color = PurpleAccent
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(colors.surface4)
                )
                HeroStat(
                    label = "Expenses",
                    value = uiState.memberStats.sumOf { it.totalOwed }.let {
                        if (it > 0) uiState.memberStats.size.toString() else "0"
                    },
                    color = PositiveGreen
                )
            }
        }
    }
}

@Composable
fun HeroStat(label: String, value: String, color: Color) {
    val colors = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textMuted
        )
    }
}

// ── Spending Chart (pure-Compose, no Vico) ───────────────────────────────────

@Composable
fun SpendingChart(history: List<Pair<String, Double>>) {
    if (history.isEmpty()) return

    val maxValue = history.maxOfOrNull { it.second } ?: 1.0
    val safeMax = if (maxValue < 0.001) 1.0 else maxValue

    // Animation state
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationPlayed = true }
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "chart_anim"
    )

    val colors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface1)
            .padding(top = 24.dp, bottom = 12.dp, start = 20.dp, end = 20.dp)
    ) {
        // The curved area chart
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val width = size.width
            val height = size.height
            val points = mutableListOf<androidx.compose.ui.geometry.Offset>()
            
            if (history.size > 1) {
                val stepX = width / (history.size - 1)
                history.forEachIndexed { index, pair ->
                    // Animate the values rising from the bottom
                    val value = pair.second * animationProgress
                    val x = index * stepX
                    val y = height - ((value / safeMax).toFloat() * height)
                    points.add(androidx.compose.ui.geometry.Offset(x, y))
                }

                val strokePath = androidx.compose.ui.graphics.Path()
                val fillPath = androidx.compose.ui.graphics.Path()

                strokePath.moveTo(points.first().x, points.first().y)
                fillPath.moveTo(points.first().x, height)
                fillPath.lineTo(points.first().x, points.first().y)

                for (i in 0 until points.size - 1) {
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    val cpX1 = (p1.x + p2.x) / 2f
                    val cpY1 = p1.y
                    val cpX2 = (p1.x + p2.x) / 2f
                    val cpY2 = p2.y
                    
                    strokePath.cubicTo(cpX1, cpY1, cpX2, cpY2, p2.x, p2.y)
                    fillPath.cubicTo(cpX1, cpY1, cpX2, cpY2, p2.x, p2.y)
                }

                fillPath.lineTo(points.last().x, height)
                fillPath.close()

                // Draw Area Fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CyanPrimary.copy(alpha = 0.4f),
                            CyanPrimary.copy(alpha = 0.01f)
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw Stroke
                drawPath(
                    path = strokePath,
                    color = CyanPrimary,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
                
                // Draw dots on data points
                points.forEach { point ->
                    drawCircle(
                        color = colors.surface1,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = CyanPrimary,
                        radius = 4.dp.toPx(),
                        center = point,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                }
            } else if (history.size == 1) {
                val y = height - ((history[0].second * animationProgress / safeMax).toFloat() * height)
                drawLine(
                    color = CyanPrimary,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(width, y),
                    strokeWidth = 3.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp), // Height matches Canvas + padding roughly
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            history.forEach { (label, _) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = colors.textMuted
                )
            }
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .background(colors.cyanPrimary, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary
        )
    }
}

// ── Member Card ───────────────────────────────────────────────────────────────

@Composable
fun MemberCard(stat: MemberSummary, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val isSettled = stat.balance >= 0
    val balanceColor = if (isSettled) PositiveGreen else NegativeRed
    val balanceBg = if (isSettled) colors.positiveGreenBg else colors.negativeRedBg

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface1)
            .bounceClick { /* Interactive feedback */ },
    ) {
        // Accent bar left edge
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(
                    color = try {
                        Color(android.graphics.Color.parseColor(stat.member.colorHex))
                    } catch (e: Exception) { CyanPrimary },
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MemberAvatar(
                    name = stat.member.name,
                    colorHex = stat.member.colorHex,
                    size = 44.dp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stat.member.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        if (stat.member.isHouseLeader) {
                            Box(
                                modifier = Modifier
                                    .background(CyanPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Leader",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = CyanPrimary
                                )
                            }
                        }
                    }
                    Text(
                        text = "Paid: RM ${"%.2f".format(stat.totalPaid)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            // Balance badge
            Box(
                modifier = Modifier
                    .background(balanceBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isSettled) "Settled"
                    else "Owes RM ${"%.2f".format(-stat.balance)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = balanceColor
                )
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
fun EmptyState() {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏠",
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No expenses this month",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tap + to add your first expense",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted
        )
    }
}

// ── Budget Progress Card ──────────────────────────────────────────────────────

@Composable
fun BudgetProgressCard(categoryBudgets: List<CategoryBudgetSummary>) {
    val colors = LocalAppColors.current
    val categoriesWithBudgets = categoryBudgets.filter { it.category.budgetLimit != null && it.category.budgetLimit > 0.0 }
    if (categoriesWithBudgets.isEmpty()) return

    val totalBudget = categoriesWithBudgets.sumOf { it.category.budgetLimit ?: 0.0 }
    val totalSpent = categoriesWithBudgets.sumOf { it.totalSpent }
    val overBudget = totalSpent > totalBudget

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface1)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Monthly Budget",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                val statusColor = if (overBudget) NegativeRed else PositiveGreen
                Text(
                    if (overBudget) "Over Budget" else "On Track",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = statusColor,
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Segmented Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(colors.surface3)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    categoriesWithBudgets.forEach { summary ->
                        val fraction = if (totalBudget > 0) (summary.totalSpent / totalBudget).toFloat() else 0f
                        if (fraction > 0f) {
                            val catColor = try {
                                Color(android.graphics.Color.parseColor(summary.category.colorHex))
                            } catch (e: Exception) { CyanPrimary }

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(fraction.coerceAtLeast(0.001f))
                                    .background(catColor)
                            )
                        }
                    }
                    if (totalSpent < totalBudget) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(((totalBudget - totalSpent) / totalBudget).toFloat())
                                .background(Color.Transparent)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: RM ${"%.2f".format(totalSpent)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary
                )
                Text(
                    text = "Limit: RM ${"%.2f".format(totalBudget)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
            }

            if (overBudget) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Exceeded by RM ${"%.2f".format(totalSpent - totalBudget)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = NegativeRed
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = colors.surface2, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            categoriesWithBudgets.take(4).forEach { summary ->
                val catColor = try {
                    Color(android.graphics.Color.parseColor(summary.category.colorHex))
                } catch (e: Exception) { CyanPrimary }
                
                val limit = summary.category.budgetLimit ?: 1.0
                val spentPercent = (summary.totalSpent / limit * 100).toInt()
                val progressFraction = (summary.totalSpent / limit).toFloat().coerceIn(0f, 1f)

                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = summary.category.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "RM ${"%.0f".format(summary.totalSpent)} / RM ${"%.0f".format(limit)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$spentPercent%",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (summary.totalSpent > limit) NegativeRed else colors.textSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = progressFraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (summary.totalSpent > limit) NegativeRed else catColor,
                        trackColor = colors.surface3
                    )
                }
            }
        }
    }
}
