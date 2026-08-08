package com.docai.kabelkopf.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.docai.kabelkopf.data.repository.GlobalSearchResult
import com.docai.kabelkopf.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<GlobalSearchResult> = emptyList(),
    val isSearching: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun updateQuery(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            if (query.isNotBlank()) {
                _state.update { it.copy(isSearching = true) }
                val results = searchRepository.searchAll(query)
                _state.update { it.copy(results = results, isSearching = false) }
            } else {
                _state.update { it.copy(results = emptyList(), isSearching = false) }
            }
        }
    }

    fun clearSearch() {
        _state.update { SearchUiState() }
        searchJob?.cancel()
    }
}
