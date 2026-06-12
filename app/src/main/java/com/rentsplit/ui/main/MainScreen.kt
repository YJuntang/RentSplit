package com.rentsplit.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.EaseInOut
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.rentsplit.ui.components.CircularGlassButton
import com.rentsplit.ui.components.progressiveBlur
import com.rentsplit.ui.components.progressiveBlurBackground
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rentsplit.ui.balances.BalancesScreen
import com.rentsplit.ui.history.HistoryScreen
import com.rentsplit.ui.history.MonthDetailScreen
import com.rentsplit.ui.home.HomeScreen
import com.rentsplit.ui.navigation.Screen
import com.rentsplit.ui.settings.SettingsScreen
import com.rentsplit.ui.categories.CategoriesScreen
import com.rentsplit.ui.theme.CyanPrimary
import com.rentsplit.ui.theme.LocalAppColors
import com.rentsplit.util.SnackbarManager
import kotlinx.coroutines.flow.collectLatest

data class NavigationItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val LocalBottomBarPadding = compositionLocalOf { 0.dp }
val LocalTopBarPadding = compositionLocalOf { 0.dp }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val haptic = LocalHapticFeedback.current
    val backgroundLayer = rememberGraphicsLayer()
    var showAddExpense by remember { mutableStateOf(false) }

    val items = listOf(
        NavigationItem("Home", Screen.Home.route, Icons.Default.Home),
        NavigationItem("Balances", Screen.Balances.route, Icons.Outlined.AccountBalance),
        NavigationItem("History", Screen.History.route, Icons.Outlined.History),
        NavigationItem("Settings", Screen.Settings.route, Icons.Default.Settings),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val topLevelRoutes = items.map { it.route }
    val showBottomBar = topLevelRoutes.any { it == currentRoute }
    val showTopBar = topLevelRoutes.any { it == currentRoute }

    val colors = LocalAppColors.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        SnackbarManager.messages.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = colors.surface0,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colors.surface2,
                    contentColor = colors.textPrimary,
                    actionColor = colors.cyanPrimary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            AnimatedVisibility(
                visible = showTopBar,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .progressiveBlur(
                            backgroundLayer = backgroundLayer,
                            isTopBar = true,
                            tintColor = colors.surface0.copy(alpha = 0.8f)
                        )
                        .background(Color.Transparent)
                ) {
                    var savedTitle by remember { mutableStateOf("RentSplit") }
                    var savedIsHome by remember { mutableStateOf(true) }
                    
                    if (currentRoute in topLevelRoutes) {
                        savedIsHome = currentRoute == Screen.Home.route
                        savedTitle = when (currentRoute) {
                            Screen.Home.route -> "RentSplit"
                            Screen.Balances.route -> "Balances"
                            Screen.History.route -> "History"
                            Screen.Settings.route -> "Settings"
                            else -> "RentSplit"
                        }
                    }
                    
                    val isHome = savedIsHome
                    val titleText = savedTitle
                    TopAppBar(
                        title = {
                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = if (isHome) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (isHome) colors.cyanPrimary else colors.textPrimary
                                )
                            )
                        },
                        actions = {
                            if (isHome) {
                                CircularGlassButton(
                                    icon = Icons.Default.Add,
                                    contentDescription = "Add Expense",
                                    onClick = {
                                        try {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } catch (e: Exception) {}
                                        showAddExpense = true
                                    },
                                    tint = colors.cyanPrimary,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent
                        )
                    )
                    
                    // Extra 32dp tail to give the progressive blur S-curve more room to seamlessly fade out over scrolling content
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .progressiveBlur(
                            backgroundLayer = backgroundLayer,
                            isTopBar = false,
                            tintColor = colors.surface0.copy(alpha = 0.8f)
                        )
                        .background(Color.Transparent)
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(36.dp))
                                .background(colors.surface2.copy(alpha = 0.90f))
                                .border(1.dp, colors.surface4, RoundedCornerShape(36.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items.forEach { item ->
                                val selected = currentDestination?.hierarchy?.any {
                                    it.route == item.route
                                } == true

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(if (selected) colors.cyanPrimary.copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable {
                                            try {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            } catch (e: Exception) {}
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (selected) colors.cyanPrimary else colors.textMuted,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalBottomBarPadding provides innerPadding.calculateBottomPadding(),
            LocalTopBarPadding provides innerPadding.calculateTopPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .progressiveBlurBackground(backgroundLayer)
                    .background(colors.surface0)
            ) {
                NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                enterTransition = {
                    val fromRoute = initialState.destination.route
                    val toRoute = targetState.destination.route
                    if (fromRoute in topLevelRoutes && toRoute in topLevelRoutes) {
                        // Tab switch → crossfade
                        fadeIn(animationSpec = tween(200))
                    } else {
                        // Push to sub-screen → slide in from the right
                        slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(250)) +
                                fadeIn(animationSpec = tween(250))
                    }
                },
                exitTransition = {
                    val fromRoute = initialState.destination.route
                    val toRoute = targetState.destination.route
                    if (fromRoute in topLevelRoutes && toRoute in topLevelRoutes) {
                        // Tab switch → crossfade
                        fadeOut(animationSpec = tween(200))
                    } else {
                        // Push to sub-screen → slide out to the left
                        slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(250)) +
                                fadeOut(animationSpec = tween(250))
                    }
                },
                popEnterTransition = {
                    // Pop (back navigation) → slide in from the left
                    slideInHorizontally(initialOffsetX = { -300 }, animationSpec = tween(250)) +
                            fadeIn(animationSpec = tween(250))
                },
                popExitTransition = {
                    // Pop (back navigation) → slide out to the right
                    slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = tween(250)) +
                            fadeOut(animationSpec = tween(250))
                }
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        showAddExpense = showAddExpense,
                        onShowAddExpenseChange = { showAddExpense = it },
                        onHistoryClick = { navController.navigate(Screen.History.route) },
                        onSettingsClick = { navController.navigate(Screen.Settings.route) }
                    )
                }
                composable(Screen.Balances.route) {
                    BalancesScreen()
                }
                composable(Screen.History.route) {
                    HistoryScreen(
                        onMonthClick = { month, year ->
                            navController.navigate(Screen.MonthDetail.createRoute(month, year))
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onCategoriesClick = { navController.navigate(Screen.Categories.route) }
                    )
                }
                composable(Screen.Categories.route) {
                    CategoriesScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(
                    route = Screen.MonthDetail.route,
                    arguments = listOf(
                        navArgument("month") { type = NavType.IntType },
                        navArgument("year") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val month = backStackEntry.arguments?.getInt("month") ?: 0
                    val year = backStackEntry.arguments?.getInt("year") ?: 0
                    MonthDetailScreen(
                        month = month,
                        year = year,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
}
