package com.elektriker.app.ui.screens.knowledge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elektriker.app.data.local.entity.KnowledgeBaseEntity
import com.elektriker.app.data.repository.KnowledgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KnowledgeUiState(
    val allEntries: List<KnowledgeBaseEntity> = emptyList(),
    val categories: List<String> = emptyList(),
    val entriesByCategory: Map<String, Int> = emptyMap(),
    val searchQuery: String = "",
    val showCategoryView: Boolean = true,
    val currentCategory: String? = null,
    val currentCategoryEntries: List<KnowledgeBaseEntity> = emptyList(),
    val selectedCategory: String? = null,
    val showFavoritesOnly: Boolean = false,
    val editingEntry: KnowledgeBaseEntity? = null
)

@HiltViewModel
class KnowledgeViewModel @Inject constructor(
    private val knowledgeRepository: KnowledgeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(KnowledgeUiState())
    val state: StateFlow<KnowledgeUiState> = _state.asStateFlow()

    init {
        loadAllEntries()
        viewModelScope.launch {
            knowledgeRepository.getAllCategories().collect { cats ->
                _state.update {
                    it.copy(categories = cats, entriesByCategory = computeCounts(it.allEntries, cats))
                }
            }
        }
    }

    private fun loadAllEntries() {
        viewModelScope.launch {
            knowledgeRepository.getAllEntries().collect { entries ->
                _state.update {
                    it.copy(
                        allEntries = entries,
                        entriesByCategory = computeCounts(entries, it.categories),
                        currentCategoryEntries = if (it.currentCategory != null)
                            entries.filter { e -> e.category == it.currentCategory }
                        else emptyList()
                    )
                }
            }
        }
    }

    private fun computeCounts(entries: List<KnowledgeBaseEntity>, cats: List<String>): Map<String, Int> {
        return if (cats.isNotEmpty()) cats.associateWith { cat ->
            entries.count { it.category == cat }
        } else entries.groupBy { it.category }.mapValues { it.value.size }
    }

    fun selectCategoryForView(category: String) {
        _state.update {
            val catEntries = it.allEntries.filter { e -> e.category == category }
            it.copy(
                showCategoryView = false,
                currentCategory = category,
                currentCategoryEntries = catEntries
            )
        }
    }

    fun backToCategories() {
        _state.update {
            it.copy(showCategoryView = true, currentCategory = null, currentCategoryEntries = emptyList())
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: String?) {
        _state.update {
            it.copy(selectedCategory = if (it.selectedCategory == category) null else category)
        }
    }

    fun toggleFavoritesFilter() {
        _state.update { it.copy(showFavoritesOnly = !it.showFavoritesOnly) }
    }

    fun toggleFavorite(id: String, isFavorite: Boolean) {
        viewModelScope.launch {
            knowledgeRepository.toggleFavorite(id, isFavorite)
        }
    }

    fun createEntry(title: String, content: String, tags: String, category: String) {
        viewModelScope.launch {
            knowledgeRepository.createEntry(
                title = title,
                content = content,
                tags = tags,
                category = category
            )
        }
    }

    fun openEntry(entry: KnowledgeBaseEntity) {
        _state.update { it.copy(editingEntry = entry) }
    }

    fun closeEntry() {
        _state.update { it.copy(editingEntry = null) }
    }

    fun updateEntry(id: String, title: String, content: String, tags: String, category: String) {
        viewModelScope.launch {
            knowledgeRepository.updateEntry(id, title, content, tags, category)
            closeEntry()
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            knowledgeRepository.deleteEntry(id)
            closeEntry()
        }
    }
}
