package com.example.data.repository

import com.example.data.db.HabitDao
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HabitWithStatus(
    val habit: Habit,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletions: Int
)

data class StreakStats(
    val currentOverallStreak: Int,
    val longestOverallStreak: Int,
    val todayCompletedCount: Int,
    val totalActiveHabits: Int,
    val todayCompletionPercentage: Float,
    val totalAllTimeCheckIns: Int
)

class HabitRepository(private val habitDao: HabitDao) {

    val activeHabits: Flow<List<Habit>> = habitDao.getAllActiveHabits()
    val allLogs: Flow<List<HabitLog>> = habitDao.getAllLogs()

    fun getLogsForDate(date: String): Flow<List<HabitLog>> = habitDao.getLogsForDate(date)

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteLogsForHabit(habit.id)
        habitDao.deleteHabit(habit)
    }

    suspend fun toggleHabitCompletion(habitId: Long, date: String): Boolean {
        val existing = habitDao.getLog(habitId, date)
        return if (existing != null && existing.isCompleted) {
            habitDao.deleteLog(habitId, date)
            false
        } else {
            habitDao.insertLog(HabitLog(habitId = habitId, date = date, isCompleted = true))
            true
        }
    }

    suspend fun populateDefaultHabitsIfEmpty() {
        val syncLogs = habitDao.getAllLogsSync()
        // If there are no habits, seed sample starter habits and recent logs
        val sampleHabits = listOf(
            Habit(
                title = "Morning Meditation",
                description = "10 minutes of mindfulness to start the day calm & focused",
                category = "Mindfulness",
                iconName = "mind",
                colorHex = "#8B5CF6",
                targetDaysPerWeek = 7
            ),
            Habit(
                title = "Drink 2.5L Water",
                description = "Stay hydrated throughout the day with regular water intake",
                category = "Health",
                iconName = "water",
                colorHex = "#06B6D4",
                targetDaysPerWeek = 7
            ),
            Habit(
                title = "30-Min Workout / Run",
                description = "Cardio, weight training, or brisk outdoor walk",
                category = "Fitness",
                iconName = "fitness",
                colorHex = "#F59E0B",
                targetDaysPerWeek = 5
            ),
            Habit(
                title = "Read 20 Pages",
                description = "Non-fiction, personal growth, or inspiring literature",
                category = "Learning",
                iconName = "book",
                colorHex = "#EC4899",
                targetDaysPerWeek = 7
            ),
            Habit(
                title = "Deep Focus Work",
                description = "90 minutes of distraction-free uninterrupted priority work",
                category = "Productivity",
                iconName = "work",
                colorHex = "#3B82F6",
                targetDaysPerWeek = 5
            )
        )
        val habitIds = habitDao.insertHabits(sampleHabits)
        
        // Seed past 5 days of realistic completion data so user immediately sees streaks and charts
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        for (dayOffset in 1..4) {
            val dateStr = today.minusDays(dayOffset.toLong()).format(formatter)
            // mark 3 or 4 habits completed on past days
            habitIds.take(4).forEach { id ->
                habitDao.insertLog(HabitLog(habitId = id, date = dateStr, isCompleted = true))
            }
        }
    }

    suspend fun clearAllData() {
        habitDao.clearAllLogs()
        habitDao.clearAllHabits()
    }

    /**
     * Accurately calculate streak for a specific habit given its list of completed dates.
     */
    fun calculateStreakForDates(completedDates: Set<String>, referenceDate: LocalDate = LocalDate.now()): Pair<Int, Int> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        if (completedDates.isEmpty()) return Pair(0, 0)

        val sortedDates = completedDates.mapNotNull {
            try { LocalDate.parse(it, formatter) } catch (e: Exception) { null }
        }.sortedDescending()

        if (sortedDates.isEmpty()) return Pair(0, 0)

        // Calculate current streak
        val todayStr = referenceDate.format(formatter)
        val yesterdayStr = referenceDate.minusDays(1).format(formatter)
        
        var currentStreak = 0
        var checkDate = if (completedDates.contains(todayStr)) {
            referenceDate
        } else if (completedDates.contains(yesterdayStr)) {
            referenceDate.minusDays(1)
        } else {
            null
        }

        if (checkDate != null) {
            while (completedDates.contains(checkDate?.format(formatter))) {
                currentStreak++
                checkDate = checkDate?.minusDays(1)
            }
        }

        // Calculate longest streak across history
        val ascendingDates = sortedDates.reversed().distinct()
        var longestStreak = 0
        var runningStreak = 0
        var prevDate: LocalDate? = null

        for (d in ascendingDates) {
            if (prevDate == null) {
                runningStreak = 1
            } else if (d == prevDate.plusDays(1)) {
                runningStreak++
            } else if (d == prevDate) {
                // duplicate date, ignore
            } else {
                runningStreak = 1
            }
            if (runningStreak > longestStreak) {
                longestStreak = runningStreak
            }
            prevDate = d
        }

        if (currentStreak > longestStreak) {
            longestStreak = currentStreak
        }

        return Pair(currentStreak, longestStreak)
    }

    /**
     * Calculate overall daily streak: days where at least 1 habit (or all habits) was completed.
     */
    fun calculateOverallStreak(allLogs: List<HabitLog>, referenceDate: LocalDate = LocalDate.now()): Pair<Int, Int> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val uniqueDaysWithCompletions = allLogs
            .filter { it.isCompleted }
            .map { it.date }
            .toSet()

        return calculateStreakForDates(uniqueDaysWithCompletions, referenceDate)
    }
}
