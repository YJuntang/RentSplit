package com.rentsplit.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rentsplit.ui.theme.*
import com.rentsplit.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onComplete()
        }
    }

    if (uiState.uiError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Setup Error") },
            text = { Text(uiState.uiError ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK", color = CyanPrimary)
                }
            },
            containerColor = LocalAppColors.current.surface1
        )
    }

    Scaffold(
        bottomBar = {
            OnboardingBottomBar(
                currentPage = pagerState.currentPage,
                totalPages = 3,
                onNext = {
                    if (pagerState.currentPage < 2) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        viewModel.completeOnboarding()
                    }
                },
                onBack = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                isSaving = uiState.isSaving
            )
        },
        containerColor = LocalAppColors.current.surface0
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            userScrollEnabled = false // Force using buttons to validate forms if needed, or allow it
        ) { page ->
            Crossfade(targetState = page, label = "page_crossfade") { current ->
                when (current) {
                    0 -> WelcomePage()
                    1 -> HouseholdPage(
                        householdName = uiState.householdName,
                        onNameChange = viewModel::updateHouseholdName,
                        onNext = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1 + 1)
                            }
                        }
                    )
                    2 -> MembersPage(
                        userName = uiState.userName,
                        onUserNameChange = viewModel::updateUserName,
                        housemates = uiState.housemates,
                        onAddHousemate = viewModel::addHousemate,
                        onUpdateHousemate = viewModel::updateHousemate,
                        onRemoveHousemate = viewModel::removeHousemate
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomePage() {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = null,
            tint = CyanPrimary,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Welcome to RentSplit",
            style = MaterialTheme.typography.headlineLarge,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "The easiest way to track expenses, manage budgets, and settle debts with your housemates.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HouseholdPage(
    householdName: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Name your Household",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Give your shared space a name (e.g., 'The Bunker', '123 Main St').",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        OutlinedTextField(
            value = householdName,
            onValueChange = onNameChange,
            label = { Text("Household Name") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = colors.surface2,
                focusedLabelColor = CyanPrimary,
                unfocusedLabelColor = colors.textSecondary,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                cursorColor = CyanPrimary
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { if (householdName.isNotBlank()) onNext() }
            )
        )
    }
}

@Composable
fun MembersPage(
    userName: String,
    onUserNameChange: (String) -> Unit,
    housemates: List<String>,
    onAddHousemate: () -> Unit,
    onUpdateHousemate: (Int, String) -> Unit,
    onRemoveHousemate: (Int) -> Unit
) {
    val colors = LocalAppColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                text = "Who lives here?",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add yourself and your housemates. You can always add more people later.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            // User's name
            OutlinedTextField(
                value = userName,
                onValueChange = onUserNameChange,
                label = { Text("Your Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = colors.surface2,
                    focusedLabelColor = CyanPrimary,
                    unfocusedLabelColor = colors.textSecondary,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = CyanPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Divider(color = colors.surface2)
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Housemates",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        itemsIndexed(housemates) { index, name ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { onUpdateHousemate(index, it) },
                    label = { Text("Housemate Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = colors.surface2,
                        focusedLabelColor = CyanPrimary,
                        unfocusedLabelColor = colors.textSecondary,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        cursorColor = CyanPrimary
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                IconButton(onClick = { onRemoveHousemate(index) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = NegativeRed
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = onAddHousemate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = CyanPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add another housemate", color = CyanPrimary)
            }
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun OnboardingBottomBar(
    currentPage: Int,
    totalPages: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    isSaving: Boolean
) {
    val colors = LocalAppColors.current
    Surface(
        color = colors.surface0,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            AnimatedVisibility(
                visible = currentPage > 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TextButton(onClick = onBack, enabled = !isSaving) {
                    Text("Back", color = colors.textSecondary)
                }
            }
            if (currentPage == 0) {
                Spacer(modifier = Modifier.width(64.dp))
            }

            // Pager Indicator
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalPages) { index ->
                    val isSelected = currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) CyanPrimary else colors.surface2)
                    )
                }
            }

            // Next / Complete Button
            Button(
                onClick = onNext,
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(24.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colors.surface0,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (currentPage == totalPages - 1) "Complete" else "Next",
                        color = colors.surface0,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
