package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.HabitItemCard
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.FlameAmber
import com.example.ui.theme.FlameGold
import com.example.ui.theme.FlameOrange
import com.example.ui.viewmodel.HabitViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val habitsWithStatus by viewModel.habitsWithStatus.collectAsState()
    val calendarDaysMap by viewModel.calendarDaysMap.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()
    val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    val selectedDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")

    // Calendar grid calculations
    val firstDayOfMonth = currentYearMonth.atDay(1)
    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 = Monday, 7 = Sunday
    val leadingEmptyDays = (firstDayOfWeek - 1) % 7

    // Monthly summary stats
    val monthLogs = calendarDaysMap.values.filter {
        it.date.year == currentYearMonth.year && it.date.monthValue == currentYearMonth.monthValue
    }
    val perfectDaysCount = monthLogs.count { it.isPerfect }
    val activeDaysCount = monthLogs.count { it.completedCount > 0 }
    val totalCheckInsThisMonth = monthLogs.sumOf { it.completedCount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen_scroll"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "Calendar History",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track daily consistency and review past entries",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Monthly Stats Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Perfect Days
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = EmeraldMint.copy(alpha = 0.1f),
                    borderColor = EmeraldMint.copy(alpha = 0.3f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, contentDescription = null, tint = EmeraldMint, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Perfect", style = MaterialTheme.typography.labelSmall, color = EmeraldMint)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$perfectDaysCount",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("100% days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Active Days
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = FlameOrange.copy(alpha = 0.1f),
                    borderColor = FlameOrange.copy(alpha = 0.3f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Active Days", style = MaterialTheme.typography.labelSmall, color = FlameOrange)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$activeDaysCount",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("consistency", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Total Check-ins
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = FlameAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Check-ins", style = MaterialTheme.typography.labelSmall, color = FlameAmber)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalCheckInsThisMonth",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("completed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 3. Month Calendar Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("month_calendar_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                            modifier = Modifier.testTag("prev_month_button")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous Month")
                        }

                        Text(
                            text = currentYearMonth.format(monthTitleFormatter),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                            modifier = Modifier.testTag("next_month_button")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next Month")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Day of Week Header Row (Mon, Tue, Wed, Thu, Fri, Sat, Sun)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayLabel ->
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid Days
                    val totalCells = leadingEmptyDays + daysInMonth
                    val rows = (totalCells + 6) / 7

                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (c in 0..6) {
                                val cellIndex = r * 7 + c
                                val dayOfMonth = cellIndex - leadingEmptyDays + 1

                                if (dayOfMonth in 1..daysInMonth) {
                                    val date = currentYearMonth.atDay(dayOfMonth)
                                    val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                    val completion = calendarDaysMap[dateStr]

                                    val isSelected = date == selectedDate
                                    val isTodayDate = date == today
                                    val isFuture = date.isAfter(today)

                                    val hasCompletions = completion != null && completion.completedCount > 0
                                    val isPerfect = completion != null && completion.isPerfect

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    isPerfect -> EmeraldMint.copy(alpha = 0.25f)
                                                    hasCompletions -> FlameAmber.copy(alpha = 0.2f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = when {
                                                    isTodayDate && !isSelected -> 2.dp
                                                    isSelected -> 2.dp
                                                    else -> 0.dp
                                                },
                                                color = when {
                                                    isSelected -> Color.White
                                                    isTodayDate -> MaterialTheme.colorScheme.primary
                                                    else -> Color.Transparent
                                                },
                                                shape = CircleShape
                                            )
                                            .clickable { viewModel.selectDate(date) }
                                            .testTag("calendar_day_$dateStr"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$dayOfMonth",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isTodayDate || isSelected || isPerfect) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> Color.White
                                                    isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                                    isPerfect -> EmeraldMint
                                                    hasCompletions -> FlameOrange
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )

                                            // Status indicator dot
                                            if (hasCompletions && !isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isPerfect) EmeraldMint else FlameAmber)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldMint))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("100% Done", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(14.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(FlameAmber))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Partial", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(14.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Missed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 4. Selected Day Habits Checklist
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Habits for ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (selectedDate != today) {
                        Text(
                            text = "Jump to Today",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.resetSelectedDateToToday() }
                                .padding(4.dp)
                                .testTag("jump_to_today_button")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedDate.format(selectedDateFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Habits for the selected date
        items(habitsWithStatus, key = { it.habit.id }) { item ->
            HabitItemCard(
                habitWithStatus = item,
                onToggle = { viewModel.toggleHabit(item.habit.id, selectedDate) },
                onEdit = { viewModel.openEditHabitDialog(item.habit) },
                onDelete = { viewModel.promptDeleteHabit(item.habit) }
            )
        }
    }
}
