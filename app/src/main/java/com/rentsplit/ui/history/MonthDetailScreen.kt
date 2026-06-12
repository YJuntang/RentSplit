package com.rentsplit.ui.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.rentsplit.ui.components.CategoryChip
import com.rentsplit.ui.components.CircularGlassButton
import androidx.compose.ui.graphics.Color
import com.rentsplit.ui.components.MemberAvatar
import com.rentsplit.ui.main.LocalBottomBarPadding
import com.rentsplit.ui.components.progressiveBlur
import com.rentsplit.ui.components.progressiveBlurBackground
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.rentsplit.ui.theme.*
import com.rentsplit.ui.theme.LocalAppColors
import com.rentsplit.ui.home.AddExpenseBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDetailScreen(
    month: Int,
    year: Int,
    viewModel: HistoryViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.monthDetailUiState.collectAsStateWithLifecycle()
    val uiError by viewModel.uiError.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val colors = LocalAppColors.current
    val backgroundLayer = rememberGraphicsLayer()
    
    var isSearchActive by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var editingExpenseId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(month, year) {
        viewModel.loadMonthDetails(month, year)
    }

    LaunchedEffect(uiError) {
        uiError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = LocalAppColors.current.surface0,
        topBar = {
            Column(
                modifier = Modifier
                    .progressiveBlur(
                        backgroundLayer = backgroundLayer,
                        isTopBar = true,
                        tintColor = colors.surface0.copy(alpha = 0.8f)
                    )
                    .background(Color.Transparent)
            ) {
                TopAppBar(
                    title = {
                        if (isSearchActive) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = viewModel::updateSearchQuery,
                                placeholder = { Text("Search expenses...", color = LocalAppColors.current.textSecondary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = LocalAppColors.current.surface4,
                                    cursorColor = CyanPrimary,
                                    focusedTextColor = LocalAppColors.current.textPrimary,
                                    unfocusedTextColor = LocalAppColors.current.textPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            Column {
                                Text(
                                    text = "${uiState.monthName} ${uiState.year}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = LocalAppColors.current.textPrimary
                                )
                                if (uiState.expenses.isNotEmpty() || searchQuery.isNotEmpty()) {
                                    Text(
                                        text = "${uiState.expenses.size} expense${if (uiState.expenses.size != 1) "s" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LocalAppColors.current.textMuted
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        CircularGlassButton(
                            icon = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            onClick = {
                                if (isSearchActive) {
                                    isSearchActive = false
                                    viewModel.updateSearchQuery("")
                                } else {
                                    onBackClick()
                                }
                            },
                            tint = LocalAppColors.current.textSecondary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    actions = {
                        if (isSearchActive) {
                            if (searchQuery.isNotEmpty()) {
                                CircularGlassButton(
                                    icon = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    onClick = { viewModel.updateSearchQuery("") },
                                    tint = LocalAppColors.current.textSecondary,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }
                        } else {
                            CircularGlassButton(
                                icon = Icons.Default.Search,
                                contentDescription = "Search expenses",
                                onClick = { isSearchActive = true },
                                tint = LocalAppColors.current.textSecondary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Box {
                                CircularGlassButton(
                                    icon = Icons.Default.MoreVert,
                                    contentDescription = "Sort options",
                                    onClick = { showSortMenu = true },
                                    tint = LocalAppColors.current.textSecondary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                    modifier = Modifier.background(LocalAppColors.current.surface0)
                                ) {
                                    val sortOptions = listOf(
                                        SortOrder.DATE_NEWEST to "Date (Newest First)",
                                        SortOrder.DATE_OLDEST to "Date (Oldest First)",
                                        SortOrder.AMOUNT_HIGHEST to "Amount (Highest First)",
                                        SortOrder.AMOUNT_LOWEST to "Amount (Lowest First)",
                                        SortOrder.NAME_A_Z to "Name (A-Z)"
                                    )
                                    sortOptions.forEach { (order, label) ->
                                        DropdownMenuItem(
                                            text = { 
                                                Text(
                                                    text = label, 
                                                    color = if (sortOrder == order) CyanPrimary else LocalAppColors.current.textPrimary,
                                                    fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                                ) 
                                            },
                                            onClick = {
                                                viewModel.updateSortOrder(order)
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                            CircularGlassButton(
                                icon = Icons.Default.Share,
                                contentDescription = "Export CSV",
                                onClick = { viewModel.exportMonthToCsv(context) },
                                tint = CyanPrimary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
                HorizontalDivider(
                    color = colors.surface3,
                    thickness = 0.5.dp
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        AnimatedContent(
            targetState = uiState.isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "MonthDetailScreenContent"
        ) { isLoading ->
            if (isLoading) {
                MonthDetailShimmer()
            } else if (uiState.expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LocalAppColors.current.surface0)
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", style = MaterialTheme.typography.displaySmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching expenses found" else "No expenses for this month",
                            color = LocalAppColors.current.textSecondary
                        )
                    }
                }
            } else {
                // Month total summary bar
                val totalAmount = uiState.expenses.filter { it.expense.splitType != com.rentsplit.data.model.SplitType.SETTLEMENT }.sumOf { it.expense.amount }
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
                        modifier = Modifier.fillMaxSize().progressiveBlurBackground(backgroundLayer),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = padding.calculateTopPadding() + 8.dp,
                            bottom = padding.calculateBottomPadding() + LocalBottomBarPadding.current + 80.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Summary card
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(LocalAppColors.current.surface1)
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Total for ${uiState.monthName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = LocalAppColors.current.textSecondary
                                    )
                                    Text(
                                        text = "RM ${"%.2f".format(totalAmount)}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = CyanPrimary
                                    )
                                }
                            }
                        }

                        items(uiState.expenses, key = { it.expense.id }) { detail ->
                            SwipeableExpenseCard(
                                modifier = Modifier.animateItem(),
                                onDelete = {
                                    viewModel.deleteExpense(detail)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "${detail.expense.title} deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoDelete()
                                        }
                                    }
                                }
                            ) {
                                ExpenseDetailCard(
                                    detail = detail,
                                    members = uiState.members,
                                    onClick = { editingExpenseId = detail.expense.id }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }

    if (editingExpenseId != null) {
        AddExpenseBottomSheet(
            expenseIdToEdit = editingExpenseId,
            onDismiss = { editingExpenseId = null },
            onSuccess = {
                viewModel.loadMonthDetails(month, year)
            }
        )
    }
}

@Composable
fun ExpenseDetailCard(
    detail: ExpenseDetail,
    members: List<com.rentsplit.data.model.Member>,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface1)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = detail.expense.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(detail.expense.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryChip(category = detail.category)
                        detail.paidByMember?.let { payer ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MemberAvatar(
                                    name = payer.name,
                                    colorHex = payer.colorHex,
                                    size = 16.dp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = payer.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textMuted
                                )
                            }
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RM ${"%.2f".format(detail.expense.amount)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CyanPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Expand/collapse button
                    if (detail.splits.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface2)
                                .clickable { expanded = !expanded }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${detail.splits.size} splits",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                            Icon(
                                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Expandable splits breakdown
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically() + fadeOut(animationSpec = tween(200))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = colors.surface3, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Split Breakdown",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    detail.splits.forEach { split ->
                        val memberName = members.find { it.id == split.memberId }?.name ?: "Member"
                        val member = members.find { it.id == split.memberId }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                member?.let {
                                    MemberAvatar(name = it.name, colorHex = it.colorHex, size = 24.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = memberName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                                Text(
                                    text = "RM ${"%.2f".format(split.amountOwed)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
