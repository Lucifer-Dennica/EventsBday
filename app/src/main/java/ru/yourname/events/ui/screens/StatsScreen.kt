package ru.yourname.events.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.yourname.events.ads.YandexBanner
import ru.yourname.events.data.model.Event
import ru.yourname.events.ui.components.SwipeNavigationBox
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    events: List<Event>,
    onBack: () -> Unit
) {
    val today = LocalDate.now()
    val stats = remember(events) { computeStats(events, today) }

    SwipeNavigationBox(
        onSwipeRight = { onBack() },
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Статистика") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { p ->
            Column(Modifier.fillMaxSize().padding(p)) {

                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (events.isEmpty()) {
                        Box(
                            Modifier.fillMaxWidth().padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Пока нет данных\nДобавьте события — и здесь появятся цифры",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // === ГЛАВНЫЕ ЦИФРЫ ===
                        BigStatCard(
                            title = "Всего событий",
                            value = stats.total.toString(),
                            emoji = "🎉",
                            gradient = listOf(Color(0xFF5B6BC0), Color(0xFF8A63A8))
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SmallStatCard(
                                Modifier.weight(1f),
                                "В этом месяце",
                                stats.thisMonthCount.toString(),
                                "📅"
                            )
                            SmallStatCard(
                                Modifier.weight(1f),
                                "Ближайшее",
                                if (stats.nearestDays == 0L) "сегодня"
                                else "через ${stats.nearestDays} дн.",
                                "⏰"
                            )
                        }

                        if (stats.avgAge != null) {
                            BigStatCard(
                                title = "Средний возраст",
                                value = "${stats.avgAge} лет",
                                emoji = "🎂",
                                gradient = listOf(Color(0xFF2E9E6B), Color(0xFF43CFA0))
                            )
                        }

                        // === ИНСАЙТЫ ===
                        Text(
                            "Инсайты",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )

                        // Самый насыщенный месяц
                        if (stats.busiestMonth != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("📈", style = MaterialTheme.typography.titleLarge)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Самый насыщенный месяц",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "${stats.busiestMonth.first} — ${stats.busiestMonth.second} событий",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Самые старшие и младшие
                        if (stats.oldest != null || stats.youngest != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("👥", style = MaterialTheme.typography.titleLarge)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Возрастные рекорды",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    if (stats.oldest != null) {
                                        Text(
                                            "Самый старший: ${stats.oldest.name} (${stats.oldest.age} лет)",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    if (stats.youngest != null) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Самый младший: ${stats.youngest.name} (${stats.youngest.age} лет)",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }

                        // Самый частый знак зодиака
                        if (stats.topZodiac != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("♈", style = MaterialTheme.typography.titleLarge)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Астрологический перекос",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "Больше всего у вас ${stats.topZodiac.first} (${stats.topZodiac.second})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Топ группа
                        if (stats.topGroup != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🏷️", style = MaterialTheme.typography.titleLarge)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Самая большая группа",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "${stats.topGroup.first} (${stats.topGroup.second} чел.)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Прогресс по чек-листам
                        if (stats.checklistTotal > 0) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("✅", style = MaterialTheme.typography.titleLarge)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Чек-листы",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "Выполнено ${stats.checklistDone} из ${stats.checklistTotal} задач",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { stats.checklistDone.toFloat() / stats.checklistTotal },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }

                        // Все группы
                        if (stats.groupCounts.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "Все группы",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    stats.groupCounts.forEach { (group, count) ->
                                        Row(
                                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                group,
                                                modifier = Modifier.weight(1f),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                "$count",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                YandexBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun BigStatCard(
    title: String,
    value: String,
    emoji: String,
    gradient: List<Color>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradient))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SmallStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    emoji: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class OldestContact(val name: String, val age: Int)

private data class Stats(
    val total: Int,
    val thisMonthCount: Int,
    val nearestDays: Long,
    val avgAge: Int?,
    val busiestMonth: Pair<String, Int>?,
    val oldest: OldestContact?,
    val youngest: OldestContact?, // НОВОЕ
    val topZodiac: Pair<String, Int>?, // НОВОЕ
    val topGroup: Pair<String, Int>?, // НОВОЕ
    val checklistDone: Int, // НОВОЕ
    val checklistTotal: Int, // НОВОЕ
    val groupCounts: List<Pair<String, Int>>
)

private fun computeStats(events: List<Event>, today: LocalDate): Stats {
    val total = events.size
    val thisMonthCount = events.count { it.month == today.monthValue }
    val nearest = events.minByOrNull { it.daysUntil(today) }
    val nearestDays = nearest?.daysUntil(today) ?: 0L

    val ages = events.mapNotNull { e -> e.year?.let { today.year - it } }.filter { it in 0..150 }
    val avgAge = if (ages.isNotEmpty()) ages.average().toInt() else null

    val monthCounts = events.groupingBy { it.month }.eachCount()
    val busiest = monthCounts.maxByOrNull { it.value }?.let { (month, count) ->
        val name = java.time.Month.of(month)
            .getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
            .replaceFirstChar { it.uppercase() }
        name to count
    }

    val ageContacts = events
        .mapNotNull { e -> e.year?.let { OldestContact(e.name, today.year - it) } }
        .filter { it.age in 0..150 }
    
    val oldest = ageContacts.maxByOrNull { it.age }
    val youngest = ageContacts.minByOrNull { it.age }

    val zodiacCounts = events.groupingBy { it.westernZodiac().label }.eachCount()
    val topZodiac = zodiacCounts.maxByOrNull { it.value }

    val groupCountsMap = events.filter { it.group.isNotBlank() }.groupingBy { it.group }.eachCount()
    val topGroup = groupCountsMap.maxByOrNull { it.value }

    val allChecklistItems = events.flatMap { it.checklist }
    val checklistTotal = allChecklistItems.size
    val checklistDone = allChecklistItems.count { it.done }

    val groupCounts = events.groupingBy { it.group }.eachCount().toList().sortedByDescending { it.second }

    return Stats(
        total = total,
        thisMonthCount = thisMonthCount,
        nearestDays = nearestDays,
        avgAge = avgAge,
        busiestMonth = busiest,
        oldest = oldest,
        youngest = youngest,
        topZodiac = topZodiac,
        topGroup = topGroup,
        checklistDone = checklistDone,
        checklistTotal = checklistTotal,
        groupCounts = groupCounts
    )
}
