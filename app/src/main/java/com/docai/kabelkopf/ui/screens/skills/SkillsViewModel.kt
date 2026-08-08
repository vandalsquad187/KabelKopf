package com.docai.kabelkopf.ui.screens.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.docai.kabelkopf.data.local.entity.SkillEntity
import com.docai.kabelkopf.data.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SkillsUiState(
    val skills: List<SkillEntity> = emptyList(),
    val totalLevel: Int = 0
)

@HiltViewModel
class SkillsViewModel @Inject constructor(
    private val skillRepository: SkillRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SkillsUiState())
    val state: StateFlow<SkillsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            skillRepository.getAllSkills().collect { skills ->
                _state.update {
                    it.copy(
                        skills = skills,
                        totalLevel = skills.sumOf { s -> s.level }
                    )
                }
            }
        }
    }
}
