package com.example

import com.example.data.db.HabitDao
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FakeHabitDao : HabitDao {
    private val habits = mutableListOf<Habit>()
    private val logs = mutableListOf<HabitLog>()

    override fun getAllActiveHabits(): Flow<List<Habit>> = flowOf(habits)
    override fun getAllHabits(): Flow<List<Habit>> = flowOf(habits)
    override suspend fun getHabitById(id: Long): Habit? = habits.find { it.id == id }
    override suspend fun insertHabit(habit: Habit): Long {
        habits.add(habit)
        return habit.id
    }
    override suspend fun insertHabits(habits: List<Habit>): List<Long> {
        this.habits.addAll(habits)
        return habits.map { it.id }
    }
    override suspend fun updateHabit(habit: Habit) {}
    override suspend fun deleteHabit(habit: Habit) { habits.remove(habit) }
    override suspend fun deleteHabitById(id: Long) { habits.removeIf { it.id == id } }

    override fun getLogsForDate(date: String): Flow<List<HabitLog>> = flowOf(logs.filter { it.date == date })
    override fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> = flowOf(logs.filter { it.habitId == habitId })
    override suspend fun getLogsForHabitSync(habitId: Long): List<HabitLog> = logs.filter { it.habitId == habitId }
    override fun getAllLogs(): Flow<List<HabitLog>> = flowOf(logs)
    override suspend fun getAllLogsSync(): List<HabitLog> = logs
    override suspend fun getLog(habitId: Long, date: String): HabitLog? = logs.find { it.habitId == habitId && it.date == date }
    override suspend fun insertLog(log: HabitLog): Long {
        logs.removeIf { it.habitId == log.habitId && it.date == log.date }
        logs.add(log)
        return log.id
    }
    override suspend fun deleteLog(habitId: Long, date: String) {
        logs.removeIf { it.habitId == habitId && it.date == date }
    }
    override suspend fun deleteLogsForHabit(habitId: Long) {
        logs.removeIf { it.habitId == habitId }
    }
    override suspend fun clearAllHabits() { habits.clear() }
    override suspend fun clearAllLogs() { logs.clear() }
}

class StreakCalculationTest {

    private lateinit var repository: HabitRepository
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @Before
    fun setup() {
        repository = HabitRepository(FakeHabitDao())
    }

    @Test
    fun testCurrentStreakCalculation_consecutiveDaysUpToToday() {
        val today = LocalDate.of(2026, 11, 2)
        val completedDates = setOf(
            today.format(formatter),
            today.minusDays(1).format(formatter),
            today.minusDays(2).format(formatter)
        )

        val (currentStreak, longestStreak) = repository.calculateStreakForDates(completedDates, today)

        assertEquals(3, currentStreak)
        assertEquals(3, longestStreak)
    }

    @Test
    fun testCurrentStreakCalculation_completedYesterdayNotYetToday() {
        // If yesterday was completed, the streak is alive (e.g. 2 days) waiting for today's check-in
        val today = LocalDate.of(2026, 11, 2)
        val completedDates = setOf(
            today.minusDays(1).format(formatter),
            today.minusDays(2).format(formatter)
        )

        val (currentStreak, longestStreak) = repository.calculateStreakForDates(completedDates, today)

        assertEquals(2, currentStreak)
        assertEquals(2, longestStreak)
    }

    @Test
    fun testCurrentStreakCalculation_brokenStreak() {
        // Missed yesterday and missed today -> streak is 0
        val today = LocalDate.of(2026, 11, 2)
        val completedDates = setOf(
            today.minusDays(3).format(formatter),
            today.minusDays(4).format(formatter)
        )

        val (currentStreak, longestStreak) = repository.calculateStreakForDates(completedDates, today)

        assertEquals(0, currentStreak)
        assertEquals(2, longestStreak)
    }
}
