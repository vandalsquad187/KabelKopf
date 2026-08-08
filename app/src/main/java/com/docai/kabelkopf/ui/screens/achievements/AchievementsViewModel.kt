package com.docai.kabelkopf.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.docai.kabelkopf.data.local.entity.AchievementEntity
import com.docai.kabelkopf.data.repository.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementsUiState(
    val achievements: List<AchievementEntity> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 0
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AchievementsUiState())
    val state: StateFlow<AchievementsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            achievementRepository.getAllAchievements().collect { achievements ->
                _state.update {
                    it.copy(
                        achievements = achievements,
                        unlockedCount = achievements.count { a -> a.isUnlocked },
                        totalCount = achievements.size
                    )
                }
            }
        }
    }
}
