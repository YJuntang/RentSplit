package com.rentsplit.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rentsplit.data.model.Category
import com.rentsplit.data.model.Household
import com.rentsplit.data.repository.RentSplitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import com.rentsplit.util.SnackbarManager

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: RentSplitRepository
) : ViewModel() {

    private val _currentHousehold = MutableStateFlow<Household?>(null)
    val currentHousehold = _currentHousehold.asStateFlow()

    private val _categories = MutableStateFlow<ImmutableList<Category>>(persistentListOf())
    val categories = _categories.asStateFlow()

    private val _uiError = MutableStateFlow<String?>(null)
    val uiError = _uiError.asStateFlow()

    init {
        loadHouseholdAndCategories()
    }

    private fun loadHouseholdAndCategories() {
        viewModelScope.launch {
            try {
                repository.getAllHouseholds().collect { households ->
                    val household = households.firstOrNull()
                    _currentHousehold.value = household
                    household?.let { h ->
                        repository.getCategoriesByHousehold(h.id).collect { cats ->
                            _categories.value = cats.toImmutableList()
                        }
                    }
                }
            } catch (e: Exception) {
                _uiError.value = "Failed to load categories"
            }
        }
    }

    fun addCategory(name: String, iconName: String, colorHex: String, budgetLimit: Double? = null) {
        viewModelScope.launch {
            try {
                val householdId = _currentHousehold.value?.id
                if (householdId == null) {
                    _uiError.value = "No household found"
                    return@launch
                }

                // Check for duplicate category name (case-insensitive)
                val isDuplicate = _categories.value.any { it.name.trim().lowercase() == name.trim().lowercase() }
                if (isDuplicate) {
                    _uiError.value = "A category with name '$name' already exists"
                    return@launch
                }

                val sortOrder = _categories.value.size
                val newCategory = Category(
                    householdId = householdId,
                    name = name,
                    iconName = iconName,
                    colorHex = colorHex,
                    budgetLimit = budgetLimit,
                    sortOrder = sortOrder
                )
                repository.insertCategory(newCategory)
                SnackbarManager.showMessage("Category \"$name\" added successfully!")
            } catch (e: Exception) {
                _uiError.value = "Failed to add category"
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                // Check if another category has the same name
                val isDuplicate = _categories.value.any { it.id != category.id && it.name.trim().lowercase() == category.name.trim().lowercase() }
                if (isDuplicate) {
                    _uiError.value = "A category with name '${category.name}' already exists"
                    return@launch
                }

                repository.updateCategory(category)
                SnackbarManager.showMessage("Category \"${category.name}\" updated successfully!")
            } catch (e: Exception) {
                _uiError.value = "Failed to update category"
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(category)
                // Adjust sort orders of remaining categories
                reorderCategories(_categories.value.filter { it.id != category.id })
                SnackbarManager.showMessage("Category \"${category.name}\" deleted successfully!")
            } catch (e: Exception) {
                _uiError.value = "Failed to delete category"
            }
        }
    }

    fun reorderCategories(orderedList: List<Category>) {
        viewModelScope.launch {
            try {
                orderedList.forEachIndexed { index, cat ->
                    if (cat.sortOrder != index) {
                        repository.updateCategory(cat.copy(sortOrder = index))
                    }
                }
            } catch (e: Exception) {
                _uiError.value = "Failed to reorder categories"
            }
        }
    }

    fun clearError() {
        _uiError.value = null
    }
}
