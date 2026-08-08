package com.docai.kabelkopf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val description: String = "",
    val iconName: String = "",
    val currentXp: Int = 0,
    val level: Int = 1,
    val maxLevel: Int = 10,
    val nextLevelXp: Int = 100
) {
    val progress: Float
        get() = if (nextLevelXp > 0) (currentXp.toFloat() / nextLevelXp).coerceIn(0f, 1f) else 1f

    val isMaxLevel: Boolean
        get() = level >= maxLevel
}
