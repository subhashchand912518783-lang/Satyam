package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.MotivationalQuotes
import com.example.data.model.Quote
import com.example.data.repository.HabitRepository
import com.example.data.repository.HabitWithStatus
import com.example.data.repository.StreakStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class Screen {
    HOME,
    HABITS,
    CALENDAR,
    STATS,
    SETTINGS
}

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

data class DayCompletion(
    val date: LocalDate,
    val dateString: String,
    val dayOfWeek: String, // "Mon", "Tue"
    val dayOfMonth: Int,
    val completedCount: Int,
    val totalCount: Int,
    val completionPercentage: Float,
    val isPerfect: Boolean,
    val isFuture: Boolean
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository
    private val prefs = application.getSharedPreferences("rozana_preferences", Context.MODE_PRIVATE)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HabitRepository(database.habitDao())

        // Prepopulate sample habits if this is the first launch
        val isFirstLaunch = prefs.getBoolean("is_first_launch", true)
        if (isFirstLaunch) {
            viewModelScope.launch {
                repository.populateDefaultHabitsIfEmpty()
                prefs.edit().putBoolean("is_first_launch", false).apply()
            }
        }
    }

    // Navigation state
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Theme Mode
    private val _themeMode = MutableStateFlow(
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // User Name
    private val _userName = MutableStateFlow(prefs.getString("user_name", "Champion") ?: "Champion")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Selected Date for viewing/checking (Defaults to today)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Current motivational quote
    private val _currentQuote = MutableStateFlow(MotivationalQuotes.getRandomQuote())
    val currentQuote: StateFlow<Quote> = _currentQuote.asStateFlow()

    // Habit filter & search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    // Editing / Creating Habit State
    private val _editingHabit = MutableStateFlow<Habit?>(null)
    val editingHabit: StateFlow<Habit?> = _editingHabit.asStateFlow()

    private val _isAddHabitDialogOpen = MutableStateFlow(false)
    val isAddHabitDialogOpen: StateFlow<Boolean> = _isAddHabitDialogOpen.asStateFlow()

    private val _habitToDelete = MutableStateFlow<Habit?>(null)
    val habitToDelete: StateFlow<Habit?> = _habitToDelete.asStateFlow()

    // Raw data flows
    val activeHabits = repository.activeHabits
    val allLogs = repository.allLogs

    // Combined UI state: Habits with completion status and streak calculation
    val habitsWithStatus: StateFlow<List<HabitWithStatus>> = combine(
        activeHabits,
        allLogs,
        _selectedDate
    ) { habits, logs, date ->
        val dateStr = date.format(dateFormatter)
        val logsByHabit = logs.groupBy { it.habitId }

        habits.map { habit ->
            val habitLogs = logsByHabit[habit.id] ?: emptyList()
            val completedDates = habitLogs.filter { it.isCompleted }.map { it.date }.toSet()
            val isCompletedToday = completedDates.contains(dateStr)

            val (curStreak, longestStreak) = repository.calculateStreakForDates(completedDates, date)

            HabitWithStatus(
                habit = habit,
                isCompletedToday = isCompletedToday,
                currentStreak = curStreak,
                longestStreak = longestStreak,
                totalCompletions = completedDates.size
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Overall Streak & Today's Summary
    val streakStats: StateFlow<StreakStats> = combine(
        habitsWithStatus,
        allLogs
    ) { habitsList, logs ->
        val todayStr = LocalDate.now().format(dateFormatter)
        val todayCompleted = habitsList.count { it.isCompletedToday }
        val totalHabits = habitsList.size
        val completionPct = if (totalHabits > 0) todayCompleted.toFloat() / totalHabits else 0f

        val (curStreak, longestStreak) = repository.calculateOverallStreak(logs, LocalDate.now())
        val allTimeCompletions = logs.count { it.isCompleted }

        StreakStats(
            currentOverallStreak = curStreak,
            longestOverallStreak = longestStreak,
            todayCompletedCount = todayCompleted,
            totalActiveHabits = totalHabits,
            todayCompletionPercentage = completionPct,
            totalAllTimeCheckIns = allTimeCompletions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StreakStats(0, 0, 0, 0, 0f, 0)
    )

    // Weekly progress (Past 7 days)
    val weeklyProgress: StateFlow<List<DayCompletion>> = combine(
        activeHabits,
        allLogs
    ) { habits, logs ->
        val today = LocalDate.now()
        val total = habits.size
        val logsByDate = logs.groupBy { it.date }

        (6 downTo 0).map { daysAgo ->
            val d = today.minusDays(daysAgo.toLong())
            val dStr = d.format(dateFormatter)
            val dayLogs = logsByDate[dStr] ?: emptyList()
            val completed = dayLogs.count { it.isCompleted }
            val pct = if (total > 0) completed.toFloat() / total else 0f

            DayCompletion(
                date = d,
                dateString = dStr,
                dayOfWeek = d.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                dayOfMonth = d.dayOfMonth,
                completedCount = completed,
                totalCount = total,
                completionPercentage = pct.coerceIn(0f, 1f),
                isPerfect = total > 0 && completed >= total,
                isFuture = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Calendar month completions map: "yyyy-MM-dd" -> DayCompletion
    val calendarDaysMap: StateFlow<Map<String, DayCompletion>> = combine(
        activeHabits,
        allLogs
    ) { habits, logs ->
        val total = habits.size
        val logsByDate = logs.groupBy { it.date }
        val today = LocalDate.now()

        val map = mutableMapOf<String, DayCompletion>()
        logsByDate.forEach { (dateStr, dateLogs) ->
            try {
                val d = LocalDate.parse(dateStr, dateFormatter)
                val completed = dateLogs.count { it.isCompleted }
                val pct = if (total > 0) completed.toFloat() / total else 0f
                map[dateStr] = DayCompletion(
                    date = d,
                    dateString = dateStr,
                    dayOfWeek = d.dayOfWeek.name.take(3),
                    dayOfMonth = d.dayOfMonth,
                    completedCount = completed,
                    totalCount = total,
                    completionPercentage = pct.coerceIn(0f, 1f),
                    isPerfect = total > 0 && completed >= total,
                    isFuture = d.isAfter(today)
                )
            } catch (_: Exception) {}
        }
        map
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // User Actions
    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun resetSelectedDateToToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun toggleHabit(habitId: Long, date: LocalDate = _selectedDate.value) {
        val dateStr = date.format(dateFormatter)
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, dateStr)
        }
    }

    fun nextQuote() {
        _currentQuote.value = MotivationalQuotes.getRandomQuote()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun openAddHabitDialog() {
        _editingHabit.value = null
        _isAddHabitDialogOpen.value = true
    }

    fun openEditHabitDialog(habit: Habit) {
        _editingHabit.value = habit
        _isAddHabitDialogOpen.value = true
    }

    fun closeAddEditDialog() {
        _isAddHabitDialogOpen.value = false
        _editingHabit.value = null
    }

    fun saveHabit(
        title: String,
        description: String,
        category: String,
        iconName: String,
        colorHex: String,
        targetDaysPerWeek: Int,
        existingId: Long = 0
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val habit = Habit(
                id = existingId,
                title = title.trim(),
                description = description.trim(),
                category = category,
                iconName = iconName,
                colorHex = colorHex,
                targetDaysPerWeek = targetDaysPerWeek
            )
            if (existingId == 0L) {
                repository.insertHabit(habit)
            } else {
                repository.updateHabit(habit)
            }
            closeAddEditDialog()
        }
    }

    fun promptDeleteHabit(habit: Habit) {
        _habitToDelete.value = habit
    }

    fun dismissDeleteDialog() {
        _habitToDelete.value = null
    }

    fun confirmDeleteHabit() {
        val habit = _habitToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteHabit(habit)
            _habitToDelete.value = null
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun setUserName(name: String) {
        val trimmed = name.trim().ifEmpty { "Champion" }
        _userName.value = trimmed
        prefs.edit().putString("user_name", trimmed).apply()
    }

    fun resetDefaultHabits() {
        viewModelScope.launch {
            repository.populateDefaultHabitsIfEmpty()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}
