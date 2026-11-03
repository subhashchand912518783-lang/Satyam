package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AddEditHabitDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.FlameOrange
import com.example.ui.viewmodel.HabitViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: HabitViewModel
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isAddDialogOpen by viewModel.isAddHabitDialogOpen.collectAsState()
    val editingHabit by viewModel.editingHabit.collectAsState()
    val habitToDelete by viewModel.habitToDelete.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    // Back handling: if on a secondary screen, back takes user to HOME
    if (currentScreen != Screen.HOME) {
        BackHandler {
            viewModel.setScreen(Screen.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = "Rozana Flame",
                            tint = FlameOrange,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ROZANA",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.5.sp
                        )
                    }
                },
                actions = {
                    // Quick Light/Dark theme toggle button in TopAppBar
                    IconButton(
                        onClick = {
                            val nextMode = if (themeMode == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
                            viewModel.setThemeMode(nextMode)
                        },
                        modifier = Modifier.testTag("topbar_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (themeMode == ThemeMode.DARK) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // 1. Home Tab
                NavigationBarItem(
                    selected = currentScreen == Screen.HOME,
                    onClick = { viewModel.setScreen(Screen.HOME) },
                    icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                    label = { Text("Home", fontWeight = if (currentScreen == Screen.HOME) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FlameOrange,
                        selectedTextColor = FlameOrange,
                        indicatorColor = FlameOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )

                // 2. Habits Tab
                NavigationBarItem(
                    selected = currentScreen == Screen.HABITS,
                    onClick = { viewModel.setScreen(Screen.HABITS) },
                    icon = { Icon(Icons.Rounded.Checklist, contentDescription = "Habits") },
                    label = { Text("Habits", fontWeight = if (currentScreen == Screen.HABITS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FlameOrange,
                        selectedTextColor = FlameOrange,
                        indicatorColor = FlameOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_habits")
                )

                // 3. Calendar Tab
                NavigationBarItem(
                    selected = currentScreen == Screen.CALENDAR,
                    onClick = { viewModel.setScreen(Screen.CALENDAR) },
                    icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Calendar") },
                    label = { Text("Calendar", fontWeight = if (currentScreen == Screen.CALENDAR) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FlameOrange,
                        selectedTextColor = FlameOrange,
                        indicatorColor = FlameOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_calendar")
                )

                // 4. Stats Tab
                NavigationBarItem(
                    selected = currentScreen == Screen.STATS,
                    onClick = { viewModel.setScreen(Screen.STATS) },
                    icon = { Icon(Icons.Rounded.BarChart, contentDescription = "Stats") },
                    label = { Text("Stats", fontWeight = if (currentScreen == Screen.STATS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FlameOrange,
                        selectedTextColor = FlameOrange,
                        indicatorColor = FlameOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_stats")
                )

                // 5. Settings Tab
                NavigationBarItem(
                    selected = currentScreen == Screen.SETTINGS,
                    onClick = { viewModel.setScreen(Screen.SETTINGS) },
                    icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontWeight = if (currentScreen == Screen.SETTINGS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FlameOrange,
                        selectedTextColor = FlameOrange,
                        indicatorColor = FlameOrange.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                when (screen) {
                    Screen.HOME -> HomeScreen(viewModel = viewModel)
                    Screen.HABITS -> HabitsScreen(viewModel = viewModel)
                    Screen.CALENDAR -> CalendarScreen(viewModel = viewModel)
                    Screen.STATS -> StatsScreen(viewModel = viewModel)
                    Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Add or Edit Habit Dialog
    if (isAddDialogOpen) {
        AddEditHabitDialog(
            initialHabit = editingHabit,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { title, desc, cat, icon, color, targetDays, id ->
                viewModel.saveHabit(title, desc, cat, icon, color, targetDays, id)
            }
        )
    }

    // Delete Habit Confirmation Dialog
    habitToDelete?.let { habit ->
        ConfirmDeleteDialog(
            habit = habit,
            onConfirm = { viewModel.confirmDeleteHabit() },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }
}
