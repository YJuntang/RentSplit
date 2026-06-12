package com.rentsplit.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rentsplit.data.model.SplitType
import com.rentsplit.ui.components.MemberAvatar
import com.rentsplit.ui.components.CategoryIconHelper
import com.rentsplit.ui.theme.*
import com.rentsplit.ui.theme.LocalAppColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import android.net.Uri
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseBottomSheet(
    expenseIdToEdit: Long? = null,
    viewModel: AddExpenseViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val titleFocusRequester = remember { FocusRequester() }
    val amountFocusRequester = remember { FocusRequester() }

    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showScanDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    fun createTempPhotoUri(): Uri {
        val tempFile = File(context.cacheDir, "receipt_temp_${System.currentTimeMillis()}.jpg")
        if (tempFile.exists()) tempFile.delete()
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempPhotoUri?.let { uri ->
                    viewModel.scanReceipt(context, uri)
                }
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = createTempPhotoUri()
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.scanReceipt(context, it)
            }
        }
    )

    LaunchedEffect(expenseIdToEdit) {
        if (expenseIdToEdit != null) {
            viewModel.loadExpenseForEdit(expenseIdToEdit)
        } else {
            viewModel.resetState()
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onSuccess()
            viewModel.resetState()
            onDismiss()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = LocalAppColors.current.surface1,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(Color(0xFF222222), RoundedCornerShape(2.dp))
            )
        },
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        val view = LocalView.current
        SideEffect {
            val dialogWindow = (view.parent as? DialogWindowProvider)?.window
            dialogWindow?.let { window ->
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }

        // Use LazyColumn as ROOT so there's no nested scroll conflict
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
        // Header
        item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.expenseId != null) "Edit Expense" else "Add Expense",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = LocalAppColors.current.textPrimary
                    )
                    Button(
                        onClick = { viewModel.saveExpense() },
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = LocalAppColors.current.surface0
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = LocalAppColors.current.surface0,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Error banner
            if (uiState.uiError != null) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NegativeRedBg)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = uiState.uiError!!,
                            color = NegativeRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Title field & OCR Scan receipt button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChange,
                        label = { Text("Title", color = LocalAppColors.current.textMuted) },
                        modifier = Modifier.weight(1f).focusRequester(titleFocusRequester),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { amountFocusRequester.requestFocus() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = LocalAppColors.current.surface4,
                            focusedLabelColor = CyanPrimary,
                            cursorColor = CyanPrimary,
                            focusedTextColor = LocalAppColors.current.textPrimary,
                            unfocusedTextColor = LocalAppColors.current.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = { showScanDialog = true },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LocalAppColors.current.surface2)
                            .border(1.dp, LocalAppColors.current.surface4, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "Scan Receipt",
                            tint = CyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            // Amount row
            item {
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text("Amount (RM)", color = LocalAppColors.current.textMuted) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            // Close keyboard
                        }
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(amountFocusRequester),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = LocalAppColors.current.surface4,
                        focusedLabelColor = CyanPrimary,
                        cursorColor = CyanPrimary,
                        focusedTextColor = LocalAppColors.current.textPrimary,
                        unfocusedTextColor = LocalAppColors.current.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Category Selection
            item {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = LocalAppColors.current.textSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    lazyItems(uiState.categories) { category ->
                        val isSelected = uiState.categoryId == category.id
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(category.colorHex))
                        } catch (e: Exception) { CyanPrimary }
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onCategoryChange(category.id) },
                            label = { Text(category.name, style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = CategoryIconHelper.getIconByName(category.iconName),
                                    contentDescription = category.name,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = catColor.copy(alpha = 0.2f),
                                selectedLabelColor = catColor,
                                selectedLeadingIconColor = catColor,
                                containerColor = LocalAppColors.current.surface2,
                                labelColor = LocalAppColors.current.textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = LocalAppColors.current.surface4,
                                selectedBorderColor = catColor.copy(alpha = 0.5f),
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.5.dp,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Date picker row
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LocalAppColors.current.surface2)
                        .clickable { showDatePicker = true }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Select Date",
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalAppColors.current.textMuted
                            )
                            Text(
                                text = uiState.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = LocalAppColors.current.textPrimary
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Paid By
            item {
                Text(
                    text = "Paid By",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = LocalAppColors.current.textSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        val isSelected = uiState.paidByMemberId == null
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onPaidByChange(null) },
                            label = { Text("All", style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = CyanPrimary,
                                containerColor = LocalAppColors.current.surface2,
                                labelColor = LocalAppColors.current.textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = LocalAppColors.current.surface4,
                                selectedBorderColor = CyanPrimary.copy(alpha = 0.5f),
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.5.dp,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                    lazyItems(uiState.members) { member ->
                        val isSelected = uiState.paidByMemberId == member.id
                        val memberColor = try {
                            Color(android.graphics.Color.parseColor(member.colorHex))
                        } catch (e: Exception) { CyanPrimary }
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onPaidByChange(member.id) },
                            label = { Text(member.name, style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = {
                                MemberAvatar(
                                    name = member.name,
                                    colorHex = member.colorHex,
                                    size = 20.dp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = memberColor.copy(alpha = 0.2f),
                                selectedLabelColor = memberColor,
                                selectedLeadingIconColor = memberColor,
                                containerColor = LocalAppColors.current.surface2,
                                labelColor = LocalAppColors.current.textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = LocalAppColors.current.surface4,
                                selectedBorderColor = memberColor.copy(alpha = 0.5f),
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.5.dp,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Split type tabs
            item {
                TabRow(
                    selectedTabIndex = if (uiState.splitType == SplitType.EQUAL) 0 else 1,
                    containerColor = LocalAppColors.current.surface2,
                    contentColor = CyanPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = uiState.splitType == SplitType.EQUAL,
                        onClick = { viewModel.onSplitTypeChange(SplitType.EQUAL) },
                        text = {
                            Text(
                                "Equal Split",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.splitType == SplitType.EQUAL) CyanPrimary else LocalAppColors.current.textMuted
                            )
                        }
                    )
                    Tab(
                        selected = uiState.splitType == SplitType.CUSTOM,
                        onClick = { viewModel.onSplitTypeChange(SplitType.CUSTOM) },
                        text = {
                            Text(
                                "Custom Split",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.splitType == SplitType.CUSTOM) CyanPrimary else LocalAppColors.current.textMuted
                            )
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Split amounts - use items() since we're already in LazyColumn
            items(uiState.memberSplits) { memberSplit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MemberAvatar(
                            name = memberSplit.member.name,
                            colorHex = memberSplit.member.colorHex,
                            size = 32.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            memberSplit.member.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalAppColors.current.textPrimary
                        )
                    }
                    if (uiState.splitType == SplitType.EQUAL) {
                        Text(
                            "RM ${memberSplit.amount}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = CyanPrimary
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (memberSplit.isManuallyEdited) {
                                IconButton(
                                    onClick = { viewModel.resetMemberSplit(memberSplit.member.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset to auto",
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            OutlinedTextField(
                                value = memberSplit.amount,
                                onValueChange = {
                                    viewModel.onMemberSplitAmountChange(memberSplit.member.id, it)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(110.dp),
                                singleLine = true,
                                prefix = { Text("RM ", color = LocalAppColors.current.textMuted, style = MaterialTheme.typography.bodySmall) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = LocalAppColors.current.surface4,
                                    focusedTextColor = LocalAppColors.current.textPrimary,
                                    unfocusedTextColor = LocalAppColors.current.textPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }
        }

        if (uiState.isScanningReceipt) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LocalAppColors.current.surface0.copy(alpha = 0.7f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(LocalAppColors.current.surface2.copy(alpha = 0.85f))
                            .border(1.5.dp, LocalAppColors.current.surface4, RoundedCornerShape(24.dp))
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                    ) {
                        CircularProgressIndicator(
                            color = CyanPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Scanning Receipt...",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = LocalAppColors.current.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Extracting details offline via ML Kit",
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalAppColors.current.textMuted
                        )
                    }
                }
            }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.date
                .atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = java.time.Instant
                            .ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.onDateChange(selectedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = CyanPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = LocalAppColors.current.textSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = LocalAppColors.current.surface2
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = LocalAppColors.current.surface2,
                    titleContentColor = LocalAppColors.current.textSecondary,
                    headlineContentColor = CyanPrimary,
                    todayContentColor = CyanPrimary,
                    todayDateBorderColor = CyanPrimary,
                    selectedDayContainerColor = CyanPrimary,
                    selectedDayContentColor = LocalAppColors.current.surface0
                )
            )
        }
    }

    // Scan Receipt Selection Dialog
    if (showScanDialog) {
        AlertDialog(
            onDismissRequest = { showScanDialog = false },
            containerColor = LocalAppColors.current.surface2,
            title = {
                Text(
                    "Scan Receipt",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LocalAppColors.current.textPrimary
                )
            },
            text = {
                Text(
                    "Choose an option to scan your receipt and auto-fill amount & title.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalAppColors.current.textSecondary
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showScanDialog = false
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = LocalAppColors.current.surface0
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Camera", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    Button(
                        onClick = {
                            showScanDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalAppColors.current.surface3,
                            contentColor = LocalAppColors.current.textPrimary
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        )
    }
}
