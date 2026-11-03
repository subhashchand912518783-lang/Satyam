package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "General", // Health, Fitness, Mind, Productivity, Learning, Lifestyle
    val iconName: String = "check",    // flame, water, book, fitness, mind, sleep, work, check
    val colorHex: String = "#FF6B00",  // hex string for color theme
    val targetDaysPerWeek: Int = 7,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class HabitCategory(val displayName: String, val defaultColorHex: String, val iconName: String) {
    HEALTH("Health", "#10B981", "water"),
    FITNESS("Fitness", "#F59E0B", "fitness"),
    MIND("Mindfulness", "#8B5CF6", "mind"),
    PRODUCTIVITY("Productivity", "#3B82F6", "work"),
    LEARNING("Learning", "#EC4899", "book"),
    LIFESTYLE("Lifestyle", "#14B8A6", "sleep")
}
