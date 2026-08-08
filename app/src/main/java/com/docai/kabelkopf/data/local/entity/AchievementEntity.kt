package com.docai.kabelkopf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconName: String = "",
    val tier: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val isBuiltIn: Boolean = true
) {
    val tierLabel: String
        get() = when (tier) {
            0 -> "Bronze"
            1 -> "Silber"
            2 -> "Gold"
            3 -> "Diamant"
            else -> ""
        }
}
