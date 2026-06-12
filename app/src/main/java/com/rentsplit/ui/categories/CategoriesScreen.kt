package com.rentsplit.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.ExperimentalFoundationApi
import com.rentsplit.data.model.Category
import com.rentsplit.ui.components.CategoryIconHelper
import com.rentsplit.ui.components.bounceClick
import com.rentsplit.ui.components.ConfirmDialog
import com.rentsplit.ui.main.LocalBottomBarPadding
// LocalHazeState removed
import com.rentsplit.ui.theme.*
import com.rentsplit.ui.theme.LocalAppColors
import com.rentsplit.ui.components.progressiveBlur
import com.rentsplit.ui.components.progressiveBlurBackground
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.rentsplit.ui.components.CircularGlassButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoriesScreen(
    onBackClick: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val uiError by viewModel.uiError.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiError) {
        uiError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val appColors = LocalAppColors.current
    val backgroundLayer = rememberGraphicsLayer()

    Scaffold(
        containerColor = appColors.surface0,
        topBar = {
            Column(
                modifier = Modifier
                    .progressiveBlur(
                        backgroundLayer = backgroundLayer,
                        isTopBar = true,
                        tintColor = appColors.surface0.copy(alpha = 0.8f)
                    )
                    .background(Color.Transparent)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Manage Categories",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = LocalAppColors.current.textPrimary
                        )
                    },
                    navigationIcon = {
                        CircularGlassButton(
                            icon = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            onClick = onBackClick,
                            tint = LocalAppColors.current.textPrimary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    actions = {
                        CircularGlassButton(
                            icon = Icons.Default.Add,
                            contentDescription = "Add Category",
                            onClick = { showAddDialog = true },
                            tint = CyanPrimary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
                HorizontalDivider(
                    color = appColors.surface3,
                    thickness = 0.5.dp
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Categories Yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LocalAppColors.current.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Click the + button to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalAppColors.current.textMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalAppColors.current.surface0)
                    .progressiveBlurBackground(backgroundLayer),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    bottom = padding.calculateBottomPadding() + LocalBottomBarPadding.current + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(categories, key = { _, category -> category.id }) { index, category ->
                    CategorySettingsCard(
                        modifier = Modifier.animateItemPlacement(),
                        category = category,
                        isFirst = index == 0,
                        isLast = index == categories.size - 1,
                        onMoveUp = {
                            val newList = categories.toMutableList()
                            val temp = newList[index]
                            newList[index] = newList[index - 1]
                            newList[index - 1] = temp
                            viewModel.reorderCategories(newList)
                        },
                        onMoveDown = {
                            val newList = categories.toMutableList()
                            val temp = newList[index]
                            newList[index] = newList[index + 1]
                            newList[index + 1] = temp
                            viewModel.reorderCategories(newList)
                        },
                        onEdit = { categoryToEdit = category },
                        onDelete = { categoryToDelete = category }
                    )
                }
            }
        }

        if (showAddDialog) {
            EditCategoryDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, icon, color, budget ->
                    viewModel.addCategory(name, icon, color, budget)
                    showAddDialog = false
                }
            )
        }

        categoryToEdit?.let { category ->
            EditCategoryDialog(
                initialName = category.name,
                initialIconName = category.iconName,
                initialColor = category.colorHex,
                initialBudget = category.budgetLimit,
                onDismiss = { categoryToEdit = null },
                onConfirm = { name, icon, color, budget ->
                    viewModel.updateCategory(
                        category.copy(
                            name = name,
                            iconName = icon,
                            colorHex = color,
                            budgetLimit = budget
                        )
                    )
                    categoryToEdit = null
                }
            )
        }

        ConfirmDialog(
            show = categoryToDelete != null,
            title = "Delete Category",
            message = "Are you sure you want to delete the category \"${categoryToDelete?.name}\"? This cannot be undone.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                categoryToDelete?.let { viewModel.deleteCategory(it) }
                categoryToDelete = null
            },
            onDismiss = {
                categoryToDelete = null
            }
        )
    }
}

@Composable
fun CategorySettingsCard(
    modifier: Modifier = Modifier,
    category: Category,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalAppColors.current
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (e: Exception) {
        CyanPrimary
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface1)
            .bounceClick { onEdit() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CategoryIconHelper.getIconByName(category.iconName),
                    contentDescription = category.name,
                    tint = categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and optional Budget
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (category.budgetLimit != null) {
                    Text(
                        text = "Budget: RM ${String.format("%.2f", category.budgetLimit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = categoryColor
                    )
                } else {
                    Text(
                        text = "No Budget Set",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                }
            }

            // Up/Down reordering arrows
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = !isFirst,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Move Up",
                        tint = if (isFirst) colors.surface3 else colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = !isLast,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Move Down",
                        tint = if (isLast) colors.surface3 else colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Action Buttons
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Category",
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Category",
                        tint = NegativeRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditCategoryDialog(
    initialName: String = "",
    initialIconName: String = "Category",
    initialColor: String = "#64748B",
    initialBudget: Double? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Double?) -> Unit
) {
    val colors = LocalAppColors.current
    var name by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(initialIconName) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var hasBudget by remember { mutableStateOf(initialBudget != null) }
    var budgetAmount by remember { mutableStateOf(initialBudget?.toString() ?: "") }

    val colorPalette = listOf(
        "#00D4FF", "#7C3AED", "#22C55E", "#FBBF24",
        "#EF4444", "#EC4899", "#06B6D4", "#84CC16",
        "#F97316", "#8B5CF6", "#14B8A6", "#F59E0B",
        "#60A5FA", "#F472B6", "#34D399", "#A78BFA"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface2,
        title = {
            Text(
                text = if (initialName.isEmpty()) "Add Category" else "Edit Category",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Name Input
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Category Name", color = colors.textMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = colors.surface4,
                            focusedLabelColor = CyanPrimary,
                            cursorColor = CyanPrimary,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Optional Budget
                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = hasBudget,
                                onCheckedChange = { hasBudget = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = CyanPrimary,
                                    checkmarkColor = colors.surface0
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Set Monthly Budget Limit",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = colors.textPrimary
                            )
                        }

                        if (hasBudget) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = budgetAmount,
                                onValueChange = { budgetAmount = it },
                                label = { Text("Budget Amount (RM)", color = colors.textMuted) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = colors.surface4,
                                    focusedLabelColor = CyanPrimary,
                                    cursorColor = CyanPrimary,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Color Selection Grid
                item {
                    Column {
                        Text(
                            "Theme Color",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        for (row in colorPalette.chunked(4)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { hex ->
                                    val color = try {
                                        Color(android.graphics.Color.parseColor(hex))
                                    } catch (e: Exception) {
                                        CyanPrimary
                                    }
                                    val isSelected = selectedColor == hex

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(color.copy(alpha = 0.25f))
                                            .border(
                                                width = if (isSelected) 2.dp else 0.dp,
                                                color = if (isSelected) color else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedColor = hex },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(color, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Icon Selection Grid
                item {
                    Column {
                        Text(
                            "Category Icon",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val activeColor = try {
                            Color(android.graphics.Color.parseColor(selectedColor))
                        } catch (e: Exception) {
                            CyanPrimary
                        }
                        for (row in CategoryIconHelper.standardIcons.chunked(4)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { iconName ->
                                    val isSelected = selectedIcon == iconName
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) activeColor.copy(alpha = 0.15f) else colors.surface3)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.dp,
                                                color = if (isSelected) activeColor else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedIcon = iconName },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = CategoryIconHelper.getIconByName(iconName),
                                            contentDescription = iconName,
                                            tint = if (isSelected) activeColor else colors.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budget = if (hasBudget) budgetAmount.toDoubleOrNull() else null
                    onConfirm(name, selectedIcon, selectedColor, budget)
                },
                enabled = name.isNotBlank() && (!hasBudget || budgetAmount.toDoubleOrNull() != null),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = colors.surface0)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        }
    )
}
