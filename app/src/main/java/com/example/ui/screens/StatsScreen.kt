package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.getIconForName
import com.example.ui.components.parseColor
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.FlameAmber
import com.example.ui.theme.FlameGold
import com.example.ui.theme.FlameOrange
import com.example.ui.viewmodel.HabitViewModel

data class Milestone(
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color
)

@Composable
fun StatsScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val habitsWithStatus by viewModel.habitsWithStatus.collectAsState()
    val streakStats by viewModel.streakStats.collectAsState()
    val weeklyProgress by viewModel.weeklyProgress.collectAsState()

    // Achievements calculation
    val milestones = listOf(
        Milestone(
            title = "First Spark",
            description = "Complete your very first habit check-in",
            isUnlocked = streakStats.totalAllTimeCheckIns >= 1,
            icon = Icons.Rounded.CheckCircle,
            accentColor = EmeraldMint
        ),
        Milestone(
            title = "3-Day Ignition",
            description = "Maintain a 3-day active streak",
            isUnlocked = streakStats.longestOverallStreak >= 3,
            icon = Icons.Rounded.LocalFireDepartment,
            accentColor = FlameAmber
        ),
        Milestone(
            title = "7-Day Champion",
            description = "Reach a full 1-week continuous streak",
            isUnlocked = streakStats.longestOverallStreak >= 7,
            icon = Icons.Rounded.Star,
            accentColor = FlameOrange
        ),
        Milestone(
            title = "21-Day Habit Former",
            description = "Hit the psychological 21-day neuroplasticity milestone",
            isUnlocked = streakStats.longestOverallStreak >= 21,
            icon = Icons.Rounded.Timeline,
            accentColor = Color(0xFF8B5CF6)
        ),
        Milestone(
            title = "Century Club",
            description = "Achieve 100 total habit completions",
            isUnlocked = streakStats.totalAllTimeCheckIns >= 100,
            icon = Icons.Rounded.EmojiEvents,
            accentColor = FlameGold
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("stats_screen_scroll"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "Analytics & Stats",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Your consistency, patterns, and unlocked badges",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. High-Level Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Current Streak Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = FlameOrange.copy(alpha = 0.12f),
                    borderColor = FlameOrange.copy(alpha = 0.35f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Current Streak", style = MaterialTheme.typography.labelMedium, color = FlameOrange)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${streakStats.currentOverallStreak} Days",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("Active chain", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Longest Streak Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = FlameGold.copy(alpha = 0.12f),
                    borderColor = FlameGold.copy(alpha = 0.35f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = FlameGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Best Streak", style = MaterialTheme.typography.labelMedium, color = FlameGold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${streakStats.longestOverallStreak} Days",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("All-time record", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 3. Weekly Progress Bar Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_chart_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Weekly Completion Rate",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Last 7 Days",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Vertical bars chart
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyProgress.forEach { day ->
                            val pct = day.completionPercentage
                            val barHeightRatio = if (pct > 0f) pct.coerceIn(0.1f, 1f) else 0.05f

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            ) {
                                // Percentage label above bar
                                Text(
                                    text = if (pct > 0f) "${(pct * 100).toInt()}%" else "0%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = if (pct >= 1f) EmeraldMint else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (pct >= 1f) FontWeight.Bold else FontWeight.Normal
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // The bar container
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    val barGradient = Brush.verticalGradient(
                                        listOf(
                                            if (pct >= 1f) EmeraldMint else FlameOrange,
                                            if (pct >= 1f) Color(0xFF059669) else FlameAmber
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(barHeightRatio)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(barGradient)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Day Label
                                Text(
                                    text = day.dayOfWeek,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Habit Breakdown (Leaderboard)
        item {
            Text(
                text = "Habit Consistency Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(habitsWithStatus.sortedByDescending { it.currentStreak }, key = { it.habit.id }) { item ->
            val habit = item.habit
            val habitColor = parseColor(habit.colorHex)
            val icon = getIconForName(habit.iconName)

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(habitColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = habitColor, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = habit.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${item.totalCompletions} total check-ins • Best: ${item.longestStreak} days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Streak pill
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = FlameOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${item.currentStreak}d",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FlameOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar towards target
                    val consistencyProgress = (item.totalCompletions / 30f).coerceIn(0.05f, 1f)
                    LinearProgressIndicator(
                        progress = { consistencyProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = habitColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        // 5. Milestones & Achievements
        item {
            Text(
                text = "Milestones & Achievements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(milestones) { milestone ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (milestone.isUnlocked) {
                    milestone.accentColor.copy(alpha = 0.08f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
                borderColor = if (milestone.isUnlocked) {
                    milestone.accentColor.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (milestone.isUnlocked) milestone.accentColor else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (milestone.isUnlocked) milestone.icon else Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = if (milestone.isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = milestone.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (milestone.isUnlocked) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "UNLOCKED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = milestone.accentColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = milestone.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
