package com.rentsplit.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rentsplit.ui.components.shimmerEffect
import com.rentsplit.ui.main.LocalBottomBarPadding
import com.rentsplit.ui.main.LocalTopBarPadding
import com.rentsplit.ui.theme.LocalAppColors

@Composable
fun HomeShimmer() {
    val colors = LocalAppColors.current
    val bottomPadding = LocalBottomBarPadding.current
    val topPadding = LocalTopBarPadding.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface0),
        contentPadding = PaddingValues(
            top = topPadding + 8.dp,
            bottom = bottomPadding + 88.dp
        ),
        userScrollEnabled = false
    ) {
        // ── Hero Card Shimmer ─────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .shimmerEffect()
            )
        }

        // ── Budget Progress Card Shimmer ──────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .shimmerEffect()
            )
        }

        // ── Section: Spending Trends Shimmer ──────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                // Section Title placeholder
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Chart Box placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .shimmerEffect()
                )
            }
        }

        // ── Section: Housemates Shimmer ───────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                // Section Title placeholder
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
        }

        items(3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface1)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // Name placeholder
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Paid amount placeholder
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }
                // Balance badge placeholder
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .shimmerEffect()
                )
            }
        }
    }
}
