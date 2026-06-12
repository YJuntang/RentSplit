package com.rentsplit.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rentsplit.data.model.Member
import com.rentsplit.ui.components.MemberAvatar
import com.rentsplit.ui.components.bounceClick
import com.rentsplit.ui.components.ConfirmDialog
import com.rentsplit.ui.main.LocalBottomBarPadding
import com.rentsplit.ui.main.LocalTopBarPadding

import com.rentsplit.ui.theme.*
import com.rentsplit.ui.theme.LocalAppColors
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onCategoriesClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val household by viewModel.currentHousehold.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val uiError by viewModel.uiError.collectAsStateWithLifecycle()

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showEditHouseholdDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<Member?>(null) }
    var memberToDelete by remember { mutableStateOf<Member?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showRestoreWarning by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showRentConfigDialog by remember { mutableStateOf(false) }
    val rentConfig by viewModel.rentConfig.collectAsStateWithLifecycle()
    val dateRangePickerState = rememberDateRangePickerState()
    val selectedMembers = remember { mutableStateListOf<Long>() }

    LaunchedEffect(members) {
        if (selectedMembers.isEmpty() && members.isNotEmpty()) {
            members.forEach { selectedMembers.add(it.id) }
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            viewModel.exportDatabaseToJson { jsonString ->
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(jsonString.toByteArray())
                }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Backup saved successfully!")
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val jsonString = inputStream?.bufferedReader().use { it?.readText() }
            if (jsonString != null) {
                viewModel.restoreDatabaseFromJson(jsonString) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Backup restored successfully!")
                    }
                }
            }
        }
    }

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
        val topPadding = LocalTopBarPadding.current
        val colors = LocalAppColors.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.surface0),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = topPadding + 8.dp,
                bottom = padding.calculateBottomPadding() + LocalBottomBarPadding.current + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Household Section ─────────────────────────────────────────
            item { SettingsSectionHeader(title = "Household", emoji = "🏠") }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface1)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Household Name",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textMuted
                            )
                            Text(
                                text = household?.name ?: "Loading…",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                        }
                        IconButton(
                            onClick = { showEditHouseholdDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(colors.surface2, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // ── Members Section ───────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsSectionHeader(title = "Members (${members.size})", emoji = "👥")
                    IconButton(
                        onClick = { showAddMemberDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(CyanPrimary.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Member",
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            items(members) { member ->
                MemberSettingsCard(
                    member = member,
                    onEdit = { memberToEdit = member },
                    onDelete = { memberToDelete = member }
                )
            }

            item { SettingsSectionHeader(title = "Categories", emoji = "🏷️", topPad = 8.dp) }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface1)
                        .clickable { onCategoriesClick() }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Manage Categories & Budgets",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Customize colors, icons, and monthly budgets",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Navigate",
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ── Manage Rent Section ────────────────────────────────────────
            item { SettingsSectionHeader(title = "Manage Rent", emoji = "🏠", topPad = 8.dp) }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface1)
                        .clickable { showRentConfigDialog = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Rent Configuration",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Set rent amount, due date & House Leader",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Navigate",
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ── Appearance Section ────────────────────────────────────────
            item { SettingsSectionHeader(title = "Appearance", emoji = "🎨", topPad = 8.dp) }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface1)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            "Theme",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Choose how RentSplit looks",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val currentTheme = preferences?.appTheme ?: "SYSTEM"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("DARK",   "🌙", "Dark"),
                                Triple("LIGHT",  "☀️", "Light"),
                                Triple("SYSTEM", "📱", "System")
                            ).forEach { (value, emoji, label) ->
                                val isSelected = currentTheme == value
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateAppTheme(value) },
                                    label = { Text("$emoji $label", style = MaterialTheme.typography.labelMedium) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = CyanPrimary,
                                        containerColor = colors.surface2,
                                        labelColor = colors.textSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = colors.surface4,
                                        selectedBorderColor = CyanPrimary.copy(alpha = 0.5f),
                                        borderWidth = 1.dp,
                                        selectedBorderWidth = 1.5.dp,
                                        enabled = true,
                                        selected = isSelected
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ── Preferences Section ───────────────────────────────────────
            item { SettingsSectionHeader(title = "Preferences", emoji = "⚙️", topPad = 8.dp) }

            item {
                preferences?.let { prefs ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surface1)
                    ) {
                        Column {
                            // Rent Reminders
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "Rent Reminders",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        "Notify on rent due date",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textMuted
                                    )
                                }
                                Switch(
                                    checked = prefs.notificationsEnabled,
                                    onCheckedChange = { viewModel.updateNotificationsEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = colors.surface0,
                                        checkedTrackColor = CyanPrimary,
                                        uncheckedThumbColor = colors.textMuted,
                                        uncheckedTrackColor = colors.surface2
                                    )
                                )
                            }

                            if (prefs.notificationsEnabled) {
                                Divider(color = colors.surface3, modifier = Modifier.padding(horizontal = 16.dp))
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Reminder Day",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.textSecondary
                                        )
                                        Text(
                                            "Day ${prefs.rentDueDay}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = CyanPrimary
                                        )
                                    }
                                    Slider(
                                        value = prefs.rentDueDay.toFloat(),
                                        onValueChange = { viewModel.updateRentDueDay(it.toInt()) },
                                        valueRange = 1f..28f,
                                        steps = 26,
                                        colors = SliderDefaults.colors(
                                            thumbColor = CyanPrimary,
                                            activeTrackColor = CyanPrimary,
                                            inactiveTrackColor = colors.surface3
                                        )
                                    )
                                }
                            }

                            Divider(color = colors.surface3, modifier = Modifier.padding(horizontal = 16.dp))

                            // Default Split Type
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Default Split",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("EQUAL", "CUSTOM").forEach { type ->
                                        val selected = prefs.defaultSplitType == type
                                        FilterChip(
                                            selected = selected,
                                            onClick = { viewModel.updateDefaultSplitType(type) },
                                            label = {
                                                Text(
                                                    type.lowercase().replaceFirstChar { it.uppercase() },
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                                selectedLabelColor = CyanPrimary,
                                                containerColor = colors.surface2,
                                                labelColor = colors.textSecondary
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                borderColor = colors.surface4,
                                                selectedBorderColor = CyanPrimary.copy(alpha = 0.5f),
                                                borderWidth = 1.dp,
                                                selectedBorderWidth = 1.5.dp,
                                                enabled = true,
                                                selected = selected
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Data & Backups Section ────────────────────────────────────
            item { SettingsSectionHeader(title = "Data & Backups", emoji = "💾", topPad = 8.dp) }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface1)
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Backup Data", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
                                Text("Save to device or share", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { createDocumentLauncher.launch("rentsplit_backup_${System.currentTimeMillis()}.json") },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) { Text("Save", color = colors.surface0, maxLines = 1) }
                                
                                Button(
                                    onClick = { 
                                        viewModel.exportDatabaseToJson { jsonString ->
                                            val file = File(context.cacheDir, "rentsplit_backup_${System.currentTimeMillis()}.json")
                                            file.writeText(jsonString)
                                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/json"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Share Backup"))
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface2, contentColor = CyanPrimary),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) { Text("Share", maxLines = 1) }
                            }
                        }
                        
                        Divider(color = colors.surface3)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Restore Data", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
                                Text("Load from a backup file", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                            }
                            Button(
                                onClick = { showRestoreWarning = true },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.surface2, contentColor = CyanPrimary)
                            ) { Text("Restore") }
                        }
                    }
                }
            }

            // ── Export Data Section ───────────────────────────────────────
            item { SettingsSectionHeader(title = "Export Data", emoji = "📤", topPad = 8.dp) }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface1)
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Date Range", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
                            Text(
                                if (dateRangePickerState.selectedStartDateMillis != null) "Selected" else "All Time",
                                color = CyanPrimary
                            )
                        }
                        Divider(color = colors.surface3)
                        
                        Text("Included Members", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            members.forEach { member ->
                                val isSelected = selectedMembers.contains(member.id)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) selectedMembers.remove(member.id) else selectedMembers.add(member.id)
                                    },
                                    label = { Text(member.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = CyanPrimary,
                                        containerColor = colors.surface2,
                                        labelColor = colors.textSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = colors.surface4,
                                        selectedBorderColor = CyanPrimary.copy(alpha = 0.5f),
                                        borderWidth = 1.dp,
                                        selectedBorderWidth = 1.5.dp,
                                        enabled = true,
                                        selected = isSelected
                                    )
                                )
                            }
                        }
                        
                        Divider(color = colors.surface3)
                        
                        Button(
                            onClick = {
                                val start = dateRangePickerState.selectedStartDateMillis ?: 0L
                                val end = dateRangePickerState.selectedEndDateMillis ?: Long.MAX_VALUE
                                viewModel.exportAdvancedCsv(start, end, selectedMembers.toList()) { csvStr ->
                                    val file = File(context.cacheDir, "rentsplit_export_${System.currentTimeMillis()}.csv")
                                    file.writeText(csvStr)
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share CSV"))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Export to CSV", color = colors.surface0)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val start = dateRangePickerState.selectedStartDateMillis ?: 0L
                                val end = dateRangePickerState.selectedEndDateMillis ?: Long.MAX_VALUE
                                viewModel.exportAdvancedMarkdown(start, end, selectedMembers.toList()) { mdStr ->
                                    val file = File(context.cacheDir, "rentsplit_export_${System.currentTimeMillis()}.md")
                                    file.writeText(mdStr)
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/markdown"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Markdown"))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.surface2, contentColor = CyanPrimary)
                        ) {
                            Text("Export to Markdown")
                        }
                    }
                }
            }

            // ── App Info ──────────────────────────────────────────────────
            item { SettingsSectionHeader(title = "About", emoji = "ℹ️", topPad = 8.dp) }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface1)
                        .padding(16.dp)
                ) {
                    Column {
                        InfoRow("App", "RentSplit")
                        Divider(color = colors.surface2, modifier = Modifier.padding(vertical = 8.dp))
                        InfoRow("Currency", "Malaysian Ringgit (RM)")
                        Divider(color = colors.surface2, modifier = Modifier.padding(vertical = 8.dp))
                        InfoRow("Version", "1.0.0")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // ── Dialogs ───────────────────────────────────────────────────────
        if (showEditHouseholdDialog) {
            EditNameDialog(
                initialName = household?.name ?: "",
                onDismiss = { showEditHouseholdDialog = false },
                onConfirm = {
                    viewModel.updateHouseholdName(it)
                    showEditHouseholdDialog = false
                }
            )
        }

        if (showAddMemberDialog) {
            EditMemberDialog(
                onDismiss = { showAddMemberDialog = false },
                onConfirm = { name, color ->
                    viewModel.addMember(name, color)
                    showAddMemberDialog = false
                }
            )
        }

        if (showRentConfigDialog) {
            RentConfigDialog(
                initialAmount = rentConfig?.amount ?: 0.0,
                initialDueDay = rentConfig?.dueDayOfMonth ?: 1,
                members = members,
                onDismiss = { showRentConfigDialog = false },
                onConfirm = { amount, dueDay, leaderId ->
                    viewModel.saveRentConfig(amount, dueDay)
                    if (leaderId != null) {
                        viewModel.setHouseLeader(leaderId)
                    }
                    showRentConfigDialog = false
                }
            )
        }

        memberToEdit?.let { member ->
            EditMemberDialog(
                initialName = member.name,
                initialColor = member.colorHex,
                onDismiss = { memberToEdit = null },
                onConfirm = { name, color ->
                    viewModel.updateMember(member.copy(name = name, colorHex = color))
                    memberToEdit = null
                }
            )
        }

        ConfirmDialog(
            show = showRestoreWarning,
            title = "Warning: Overwrite Data",
            message = "This will overwrite all current expense records and households. Are you sure you want to continue?",
            confirmText = "Restore",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                showRestoreWarning = false
                restoreLauncher.launch("application/json")
            },
            onDismiss = {
                showRestoreWarning = false
            }
        )

        ConfirmDialog(
            show = memberToDelete != null,
            title = "Delete Member",
            message = "Are you sure you want to delete \"${memberToDelete?.name}\"? This will also affect their balances and expense distributions.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                memberToDelete?.let { viewModel.deleteMember(it) }
                memberToDelete = null
            },
            onDismiss = {
                memberToDelete = null
            }
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("OK", color = CyanPrimary) }
                }
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    title = { Text("Select Date Range", modifier = Modifier.padding(16.dp)) }
                )
            }
        }
    }
}

// ── Helper Composables ────────────────────────────────────────────────────────

@Composable
fun SettingsSectionHeader(title: String, emoji: String, topPad: Dp = 0.dp) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.padding(top = topPad, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textSecondary
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = colors.textSecondary)
    }
}

@Composable
fun MemberSettingsCard(member: Member, onEdit: () -> Unit, onDelete: () -> Unit) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface1)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MemberAvatar(name = member.name, colorHex = member.colorHex, size = 40.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        member.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                    if (member.isHouseLeader) {
                        Text(
                            "House Leader",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanPrimary
                        )
                    }
                }
            }
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
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
                        contentDescription = "Delete",
                        tint = NegativeRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditNameDialog(initialName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val colors = LocalAppColors.current
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface2,
        title = {
            Text(
                "Edit Household Name",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name.trim()) }, enabled = name.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)) {
                Text("Save", color = colors.surface0)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textSecondary) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentConfigDialog(
    initialAmount: Double,
    initialDueDay: Int,
    members: List<Member>,
    onDismiss: () -> Unit,
    onConfirm: (Double, Int, Long?) -> Unit
) {
    val colors = LocalAppColors.current
    var amount by remember { mutableStateOf(if (initialAmount > 0) initialAmount.toString() else "") }
    var dueDay by remember { mutableStateOf(initialDueDay.toFloat()) }
    var selectedLeaderId by remember { mutableStateOf(members.find { it.isHouseLeader }?.id) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface2,
        title = {
            Text(
                "Rent Configuration",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monthly Rent (RM)", color = colors.textMuted) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = colors.surface4,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedLabelColor = CyanPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Due Day: ${dueDay.toInt()}", color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = dueDay,
                        onValueChange = { dueDay = it },
                        valueRange = 1f..28f,
                        steps = 26,
                        colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = members.find { it.id == selectedLeaderId }?.name ?: "Select House Leader",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("House Leader", color = colors.textMuted) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = colors.surface4,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedLabelColor = CyanPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(colors.surface2)
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name, color = colors.textPrimary) },
                                onClick = {
                                    selectedLeaderId = member.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    onConfirm(amt, dueDay.toInt(), selectedLeaderId)
                },
                enabled = amount.isNotBlank() && selectedLeaderId != null,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Save", color = colors.surface0)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textSecondary) }
        }
    )
}


@Composable
fun EditMemberDialog(
    initialName: String = "",
    initialColor: String = "#00D4FF",
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedColor by remember { mutableStateOf(initialColor) }

    // 16-color expanded palette
    val colorPalette = listOf(
        "#00D4FF", "#7C3AED", "#22C55E", "#F59E0B",
        "#EF4444", "#EC4899", "#06B6D4", "#84CC16",
        "#F97316", "#8B5CF6", "#14B8A6", "#FBBF24",
        "#60A5FA", "#F472B6", "#34D399", "#A78BFA"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface2,
        title = {
            Text(
                if (initialName.isEmpty()) "Add Member" else "Edit Member",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Surface4,
                        focusedLabelColor = CyanPrimary,
                        cursorColor = CyanPrimary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                // 4-column color grid
                for (row in colorPalette.chunked(4)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { hex ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) { CyanPrimary }
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
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedColor) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Surface0)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
