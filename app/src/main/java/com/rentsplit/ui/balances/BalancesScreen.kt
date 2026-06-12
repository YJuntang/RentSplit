package com.rentsplit.ui.balances

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch
import androidx.compose.foundation.ExperimentalFoundationApi
import com.rentsplit.ui.components.MemberAvatar
import com.rentsplit.ui.components.bounceClick
import com.rentsplit.ui.main.LocalBottomBarPadding
import com.rentsplit.ui.main.LocalTopBarPadding
import com.rentsplit.ui.theme.*
import com.rentsplit.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BalancesScreen(
    viewModel: BalancesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

    Scaffold(
        containerColor = colors.surface0
    ) { padding ->
        AnimatedContent(
            targetState = uiState.isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "BalancesScreenContent"
        ) { isLoading ->
            if (isLoading) {
                BalancesShimmer()
            } else if (uiState.balances.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.surface0)
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // All settled illustration
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(PositiveGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✅", style = MaterialTheme.typography.displaySmall)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "All settled up!",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = PositiveGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No outstanding balances",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add expenses and they'll appear here",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }
                }
            } else {
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
                            .background(colors.surface0),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = topPadding + 8.dp,
                            bottom = LocalBottomBarPadding.current + 80.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total outstanding summary
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                WarningAmber.copy(alpha = 0.15f),
                                                NegativeRed.copy(alpha = 0.08f)
                                            )
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Total Outstanding",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = colors.textMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "RM ${"%.2f".format(uiState.totalOwed)}",
                                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                        color = WarningAmber
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${uiState.balances.size} transaction${if (uiState.balances.size != 1) "s" else ""} to settle",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }

                        // Section header
                        item {
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(18.dp)
                                        .background(WarningAmber, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Pending Settlements",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textSecondary
                                )
                            }
                        }

                        // Balance cards
                        itemsIndexed(uiState.balances, key = { _, entry -> "${entry.fromMember.id}_${entry.toMember.id}" }) { index, entry ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { visible = true }
                            AnimatedVisibility(
                                visible = visible,
                                modifier = Modifier.animateItem(),
                                enter = slideInVertically(
                                    initialOffsetY = { 50 },
                                    animationSpec = tween(300, delayMillis = index * 70)
                                ) + fadeIn(animationSpec = tween(300, delayMillis = index * 70))
                            ) {
                                BalanceCard(
                                    entry = entry,
                                    onSettleUp = { viewModel.settleUp(entry) }
                                )
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
fun BalanceCard(
    entry: BalanceEntry,
    onSettleUp: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Settlement") },
            text = { Text("Are you sure you want to settle RM ${"%.2f".format(entry.amount)} from ${entry.fromMember.name} to ${entry.toMember.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onSettleUp()
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(LocalAppColors.current.surface1)
    ) {
        // Left accent bar in the debtor's color
        val debtorColor = try {
            Color(android.graphics.Color.parseColor(entry.fromMember.colorHex))
        } catch (e: Exception) { NegativeRed }

        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    debtorColor.copy(alpha = 0.7f),
                    RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                )
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // Main row: avatar → arrow → avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Debtor (owes)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    MemberAvatar(
                        name = entry.fromMember.name,
                        colorHex = entry.fromMember.colorHex,
                        size = 48.dp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = entry.fromMember.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = LocalAppColors.current.textPrimary
                    )
                    Text(
                        text = "owes",
                        style = MaterialTheme.typography.labelSmall,
                        color = NegativeRed
                    )
                }

                // Arrow + amount in the middle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .background(NegativeRed.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "RM ${"%.2f".format(entry.amount)}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = NegativeRed
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.headlineMedium,
                        color = LocalAppColors.current.textMuted
                    )
                }

                // Creditor (is owed)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    MemberAvatar(
                        name = entry.toMember.name,
                        colorHex = entry.toMember.colorHex,
                        size = 48.dp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = entry.toMember.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = LocalAppColors.current.textPrimary
                    )
                    Text(
                        text = "receives",
                        style = MaterialTheme.typography.labelSmall,
                        color = PositiveGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = LocalAppColors.current.surface2, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Settle Up",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = LocalAppColors.current.surface0
                )
            }
        }
    }
}
