package com.katiba.app.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katiba.app.data.model.DailyContent
import com.katiba.app.data.model.Lesson
import com.katiba.app.data.repository.SampleDataRepository
import com.katiba.app.ui.theme.KatibaColors
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// 10 Different gradient backgrounds for Clause of the Day card
private val clauseGradients = listOf(
    // Gradient 1: Earth tones (Original)
    listOf(Color(0xFF8B7355), Color(0xFF5D4037), Color(0xFF212121)),
    // Gradient 2: Ocean depths
    listOf(Color(0xFF1A5276), Color(0xFF2E4053), Color(0xFF17202A)),
    // Gradient 3: Forest green
    listOf(Color(0xFF1E8449), Color(0xFF145A32), Color(0xFF0B2311)),
    // Gradient 4: Royal purple
    listOf(Color(0xFF6C3483), Color(0xFF4A235A), Color(0xFF1A0A1F)),
    // Gradient 5: Sunset warmth
    listOf(Color(0xFFD35400), Color(0xFF873600), Color(0xFF2C1200)),
    // Gradient 6: Kenya flag inspired
    listOf(Color(0xFF333333), Color(0xFF8B0000), Color(0xFF006600)),
    // Gradient 7: Midnight blue
    listOf(Color(0xFF2C3E50), Color(0xFF1B2631), Color(0xFF0D1318)),
    // Gradient 8: Bronze age
    listOf(Color(0xFFB7950B), Color(0xFF7D6608), Color(0xFF3D3103)),
    // Gradient 9: Maasai red
    listOf(Color(0xFFCD5C5C), Color(0xFF8B0000), Color(0xFF2D0000)),
    // Gradient 10: Savanna gold
    listOf(Color(0xFFDAA520), Color(0xFF8B6914), Color(0xFF3D2E09))
)

/**
 * Get gradient index based on the current date (changes at midnight)
 */
private fun getGradientForDay(): List<Color> {
    val now = Clock.System.now()
    val localDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val dayOfYear = localDate.dayOfYear
    val gradientIndex = dayOfYear % clauseGradients.size
    return clauseGradients[gradientIndex]
}

enum class HomeTab {
    TODAY, COMMUNITY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onClauseCardClick: () -> Unit,
    onAIDescriptionCardClick: () -> Unit,
    onTipsCardClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onMzalendoClick: () -> Unit,
    onResumeLesson: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyContent = remember { SampleDataRepository.getDailyContent() }
    val lessons = remember { SampleDataRepository.getLessons() }
    val currentLesson = remember(lessons) { lessons.find { it.isCurrent } }
    val completedLessons = remember(lessons) { lessons.count { it.isCompleted } }
    val totalLessons = remember(lessons) { lessons.size }
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf(HomeTab.TODAY) }
    var showStreakSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    var isClauseExpanded by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top App Bar
            HomeTopBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onStreakClick = { showStreakSheet = true },
                onNotificationsClick = onNotificationsClick
            )
            
            if (selectedTab == HomeTab.TODAY) {
                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Greeting - now part of scrollable content
                    Text(
                        text = "${getGreeting()}, Qurlarmah",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Card 1: Clause of the Day (Hero Card)
                    ClauseOfTheDayCard(
                        dailyContent = dailyContent,
                        onClick = { isClauseExpanded = true }
                    )
                    
                    // Card 2: AI Description + Video
                    AIDescriptionCard(
                        dailyContent = dailyContent,
                        onClick = onAIDescriptionCardClick
                    )
                    
                    // Card 3: Next Steps & Tips
                    TipsCard(
                        dailyContent = dailyContent,
                        onClick = onTipsCardClick
                    )
                    
                    // Card 4: Learning Progress
                    currentLesson?.let { lesson ->
                        LearningProgressCard(
                            currentLesson = lesson,
                            lessonNumber = completedLessons + 1,
                            completionPercentage = (completedLessons * 100) / totalLessons,
                            onResumeClick = { onResumeLesson(lesson.id) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                CommunityScreen()
            }
        }

        // Mzalendo AI Assistant FAB
        if (selectedTab == HomeTab.TODAY && !isClauseExpanded) {
            FloatingActionButton(
                onClick = onMzalendoClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                containerColor = KatibaColors.KenyaGreen
            ) {
                Icon(
                    imageVector = MzalendoIcon,
                    contentDescription = "Ask Mzalendo",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isClauseExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically)
        ) {
            ExpandedClauseView(
                dailyContent = dailyContent,
                onClose = { isClauseExpanded = false }
            )
        }
    }

    if (showStreakSheet) {
        StreakBottomSheet(
            sheetState = sheetState,
            onDismiss = { showStreakSheet = false }
        )
    }
}

private fun getGreeting(): String {
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    return when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onStreakClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    streakCount: Int = 9 // TODO: Get from user profile
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .clickable { onTabSelected(HomeTab.TODAY) },
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == HomeTab.TODAY) Color.Black else Color.Gray
                    )
                    if (selectedTab == HomeTab.TODAY) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(KatibaColors.KenyaRed)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .clickable { onTabSelected(HomeTab.COMMUNITY) },
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Community",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == HomeTab.COMMUNITY) Color.Black else Color.Gray
                    )
                    if (selectedTab == HomeTab.COMMUNITY) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(KatibaColors.KenyaRed)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onStreakClick() }
                ) {
                    // Bolt icon: yellow fill when streak > 0, black outline when streak = 0
                    Icon(
                        imageVector = if (streakCount > 0) BoltIconFilled else BoltIconOutline,
                        contentDescription = "Streak",
                        tint = if (streakCount > 0) Color(0xFFFFD700) else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "$streakCount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.Black
                    )
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Info, contentDescription = null)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Share, contentDescription = null)
                }
            }

            Text(
                text = "Streaks help you build your daily Katiba habit.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(32.dp))
                        Text("9", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("App Streak", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("17 Best", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(modifier = Modifier.width(1.dp).height(80.dp).background(Color.LightGray))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(32.dp))
                        Text("0", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Daily Refresh Streak", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("10 Best", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("6 weeks in a row", fontWeight = FontWeight.Bold)
            Text("11 days in the Katiba App this year", color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))
            
            // Simple calendar representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("S", "M", "T", "W", "T", "F", "S")
                val dates = listOf("11", "12", "13", "14", "15", "16", "17")
                
                days.zip(dates).forEach { (day, date) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(day, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .let { 
                                    if (date == "12") it.background(Color.Transparent).border(1.dp, Color.Black, CircleShape)
                                    else it
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(date)
                            if (date == "11" || date == "12") {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(4.dp)
                                        .background(Color.Red, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ClauseOfTheDayCard(
    dailyContent: DailyContent,
    onClick: () -> Unit
) {
    val gradientColors = remember { getGradientForDay() }
    var isLiked by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            )
            .clickable(onClick = onClick)
    ) {
        // Kenyan flag colored ribbons on the left edge (book spine effect)
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(12.dp)
        ) {
            // Black ribbon
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(KatibaColors.KenyaBlack)
            )
            // Red ribbon
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(KatibaColors.KenyaRed)
            )
            // Green ribbon
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(KatibaColors.KenyaGreen)
            )
        }

        // Overlay for better text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.15f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 28.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Badge Section - Frosted glass style with gavel icon
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Frosted glass badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Gavel icon
                        Icon(
                            imageVector = GavelIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "CLAUSE OF THE DAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
                
                Text(
                    text = "Article ${dailyContent.articleNumber}, Clause ${dailyContent.clause.number}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Middle Quote - Text sized to fit within card
            Text(
                text = "\"${dailyContent.clause.text}\"",
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                ),
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )

            // Bottom Section
            ClauseBottomSection(
                dailyContent = dailyContent,
                isLiked = isLiked,
                isBookmarked = isBookmarked,
                onShareClick = { /* TODO: Implement share functionality */ },
                onLikeClick = { isLiked = !isLiked },
                onBookmarkClick = { isBookmarked = !isBookmarked }
            )
        }
    }
}

@Composable
private fun ClauseBottomSection(
    dailyContent: DailyContent,
    modifier: Modifier = Modifier,
    isLiked: Boolean = false,
    isBookmarked: Boolean = false,
    onShareClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Citation
        Text(
            text = "Article ${dailyContent.articleNumber}, Section 1 • ${dailyContent.chapterTitle}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Social info
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(2) { index ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .offset(x = (index * -8).dp)
                            .clip(CircleShape)
                            .background(if (index == 0) Color(0xFFFFDAB9) else Color(0xFFE6E6FA))
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = (-16).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A1A))
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+24",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            // Action buttons - now clickable
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        tint = if (isLiked) Color(0xFFFF6B6B) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (isBookmarked) BookmarkFilledIcon else BookmarkIcon,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark",
                        tint = if (isBookmarked) Color(0xFFFFD700) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AIDescriptionCard(
    dailyContent: DailyContent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Badge with water drop icon
                Surface(
                    color = Color(0xFFE8E8E8),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = WaterDropIcon,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                }

                // Title label
                Text(
                    text = "Deep Dive",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                // Main title
                Text(
                    text = "What This Clause Means",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Duration with play icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "▶",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                    Text(
                        text = "2-5 min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            }
            
            // Right - Placeholder image (rounded square with educator placeholder)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFB8D4E3),
                                Color(0xFF9BC4D9)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder person icon
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Educator",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
private fun TipsCard(
    dailyContent: DailyContent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title label
                Text(
                    text = "Application",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                // Main title
                Text(
                    text = "Apply This in Your Life",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Duration with play icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "▶",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                    Text(
                        text = "4-6 min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            }

            // Right - Placeholder image with wave pattern and hand icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFD4A5A5))
                    .drawBehind {
                        // Draw diagonal wave stripes
                        val stripeWidth = 20.dp.toPx()
                        var startX = -size.width
                        while (startX < size.width * 2) {
                            drawLine(
                                color = Color(0xFFBE8F8F),
                                start = Offset(startX, size.height),
                                end = Offset(startX + size.height, 0f),
                                strokeWidth = stripeWidth
                            )
                            startX += stripeWidth * 2
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Circular icon container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    // Hand/wave emoji or icon
                    Text(
                        text = "👋",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }
}

/**
 * Card 4: Learning Progress Card
 * Shows the user's current lesson progress with a circular progress indicator
 * and abstract watermark designs in the background
 */
@Composable
private fun LearningProgressCard(
    currentLesson: Lesson,
    lessonNumber: Int,
    completionPercentage: Int,
    onResumeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Abstract watermark designs - subtle geometric patterns
                    val watermarkColor = Color(0xFFE8F5E9) // Very light green

                    // Draw large abstract circles
                    drawCircle(
                        color = watermarkColor,
                        radius = 120.dp.toPx(),
                        center = Offset(size.width * 0.85f, size.height * 0.2f)
                    )
                    drawCircle(
                        color = watermarkColor.copy(alpha = 0.5f),
                        radius = 80.dp.toPx(),
                        center = Offset(size.width * 0.1f, size.height * 0.9f)
                    )

                    // Draw abstract curved lines
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                    drawLine(
                        color = watermarkColor,
                        start = Offset(0f, size.height * 0.3f),
                        end = Offset(size.width * 0.4f, size.height * 0.1f),
                        strokeWidth = 3.dp.toPx(),
                        pathEffect = pathEffect
                    )
                    drawLine(
                        color = watermarkColor,
                        start = Offset(size.width * 0.6f, size.height * 0.9f),
                        end = Offset(size.width, size.height * 0.6f),
                        strokeWidth = 3.dp.toPx(),
                        pathEffect = pathEffect
                    )

                    // Small decorative dots
                    val dotColor = Color(0xFFDCEDC8)
                    drawCircle(
                        color = dotColor,
                        radius = 6.dp.toPx(),
                        center = Offset(size.width * 0.2f, size.height * 0.25f)
                    )
                    drawCircle(
                        color = dotColor,
                        radius = 4.dp.toPx(),
                        center = Offset(size.width * 0.75f, size.height * 0.75f)
                    )
                    drawCircle(
                        color = dotColor,
                        radius = 8.dp.toPx(),
                        center = Offset(size.width * 0.5f, size.height * 0.15f)
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Lesson label
                    Text(
                        text = "LESSON $lessonNumber",
                        style = MaterialTheme.typography.labelSmall,
                        color = KatibaColors.KenyaRed,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    // Lesson title
                    Text(
                        text = currentLesson.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Lesson description
                    Text(
                        text = currentLesson.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Resume button
                    Button(
                        onClick = onResumeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KatibaColors.KenyaRed
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Resume",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right - Circular progress indicator
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background circle
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFE0E0E0),
                        strokeWidth = 6.dp,
                        trackColor = Color.Transparent
                    )
                    // Progress circle
                    CircularProgressIndicator(
                        progress = { completionPercentage / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = KatibaColors.KenyaGreen,
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round,
                        trackColor = Color.Transparent
                    )
                    // Percentage text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$completionPercentage",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = KatibaColors.KenyaGreen
                        )
                        Text(
                            text = "%",
                            style = MaterialTheme.typography.labelSmall,
                            color = KatibaColors.KenyaGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedClauseView(
    dailyContent: DailyContent,
    onClose: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val gradientColors = remember { getGradientForDay() }
    var isLiked by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            )
    ) {
        // Background overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        // Main content in a Column to prevent overlap
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Read Full Chapter") },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Create Image") },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Compare Versions") },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("See Past Days") },
                            onClick = { showMenu = false }
                        )
                    }
                }
            }

            // Centered Content - uses weight to fill available space between top bar and bottom section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Clause of the Day",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Article ${dailyContent.articleNumber}, Clause ${dailyContent.clause.number}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "\"${dailyContent.clause.text}\"",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        lineHeight = 42.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom buttons/components - fixed at bottom, never overlapped
            ClauseBottomSection(
                dailyContent = dailyContent,
                isLiked = isLiked,
                isBookmarked = isBookmarked,
                onShareClick = { /* TODO: Implement share functionality */ },
                onLikeClick = { isLiked = !isLiked },
                onBookmarkClick = { isBookmarked = !isBookmarked },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 64.dp)
            )
        }
    }
}

// Bolt icon filled (for streak > 0) - Based on the SVG from /composeResources/files/bolt.svg
private val BoltIconFilled: ImageVector
    get() = ImageVector.Builder(
        name = "BoltFilled",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Path from bolt.svg: M13 3l0 7l6 0l-8 11l0 -7l-6 0l8 -11
        path(fill = SolidColor(Color.Black)) {
            moveTo(13f, 3f)
            lineTo(13f, 10f)
            lineTo(19f, 10f)
            lineTo(11f, 21f)
            lineTo(11f, 14f)
            lineTo(5f, 14f)
            lineTo(13f, 3f)
            close()
        }
    }.build()

// Bolt icon outline (for streak = 0) - Based on the SVG from /composeResources/files/bolt.svg
private val BoltIconOutline: ImageVector
    get() = ImageVector.Builder(
        name = "BoltOutline",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Path from bolt.svg: M13 3l0 7l6 0l-8 11l0 -7l-6 0l8 -11
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(13f, 3f)
            lineTo(13f, 10f)
            lineTo(19f, 10f)
            lineTo(11f, 21f)
            lineTo(11f, 14f)
            lineTo(5f, 14f)
            lineTo(13f, 3f)
            close()
        }
    }.build()

// Gavel icon for the clause of the day badge - from /composeResources/files/gavel.svg
private val GavelIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Gavel",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Path 1: Handle with curved end - M13 10l7.383 7.418c.823 .82 .823 2.148 0 2.967a2.11 2.11 0 0 1 -2.976 0l-7.407 -7.385
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(13f, 10f)
            lineTo(20.383f, 17.418f)
            curveTo(21.206f, 18.238f, 21.206f, 19.566f, 20.383f, 20.385f)
            curveTo(19.56f, 21.205f, 18.23f, 21.205f, 17.407f, 20.385f)
            lineTo(10f, 13f)
        }
        // Path 2: Small diagonal line - M6 9l4 4
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(6f, 9f)
            lineTo(10f, 13f)
        }
        // Path 3: Small diagonal line - M13 10l-4 -4
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(13f, 10f)
            lineTo(9f, 6f)
        }
        // Path 4: Base line - M3 21h7
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(3f, 21f)
            horizontalLineTo(10f)
        }
        // Path 5: Main gavel head shape
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(6.793f, 15.793f)
            lineTo(3.207f, 12.207f)
            curveTo(2.817f, 11.817f, 2.817f, 11.183f, 3.207f, 10.793f)
            lineTo(5.5f, 8.5f)
            lineTo(6f, 9f)
            lineTo(9f, 6f)
            lineTo(8.5f, 5.5f)
            lineTo(10.793f, 3.207f)
            curveTo(11.183f, 2.817f, 11.817f, 2.817f, 12.207f, 3.207f)
            lineTo(15.793f, 6.793f)
            curveTo(16.183f, 7.183f, 16.183f, 7.817f, 15.793f, 8.207f)
            lineTo(13.5f, 10.5f)
            lineTo(13f, 10f)
            lineTo(10f, 13f)
            lineTo(10.5f, 13.5f)
            lineTo(8.207f, 15.793f)
            curveTo(7.817f, 16.183f, 7.183f, 16.183f, 6.793f, 15.793f)
            close()
        }
    }.build()

// Water drop icon for the Deep Dive card badge
private val WaterDropIcon: ImageVector
    get() = ImageVector.Builder(
        name = "WaterDrop",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2f)
            curveTo(12f, 2f, 6f, 9f, 6f, 14f)
            curveTo(6f, 17.31f, 8.69f, 20f, 12f, 20f)
            curveTo(15.31f, 20f, 18f, 17.31f, 18f, 14f)
            curveTo(18f, 9f, 12f, 2f, 12f, 2f)
            close()
        }
    }.build()

private val BookmarkIcon: ImageVector
    get() = ImageVector.Builder(
        name = "BookmarkOutline",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(17f, 3f)
            horizontalLineTo(7f)
            curveTo(5.9f, 3f, 5f, 3.9f, 5f, 5f)
            verticalLineTo(21f)
            lineTo(12f, 18f)
            lineTo(19f, 21f)
            verticalLineTo(5f)
            curveTo(19f, 3.9f, 18.1f, 3f, 17f, 3f)
            close()
        }
    }.build()

private val BookmarkFilledIcon: ImageVector
    get() = ImageVector.Builder(
        name = "BookmarkFilled",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(17f, 3f)
            horizontalLineTo(7f)
            curveTo(5.9f, 3f, 5f, 3.9f, 5f, 5f)
            verticalLineTo(21f)
            lineTo(12f, 18f)
            lineTo(19f, 21f)
            verticalLineTo(5f)
            curveTo(19f, 3.9f, 18.1f, 3f, 17f, 3f)
            close()
        }
    }.build()

// Mzalendo AI Assistant Icon (Chat/Smart Assistant)
private val MzalendoIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Mzalendo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Chat bubble with AI sparkle
        path(fill = SolidColor(Color.Black)) {
            // Chat bubble
            moveTo(20f, 2f)
            horizontalLineTo(4f)
            curveTo(2.9f, 2f, 2f, 2.9f, 2f, 4f)
            verticalLineTo(16f)
            curveTo(2f, 17.1f, 2.9f, 18f, 4f, 18f)
            horizontalLineTo(8f)
            lineTo(12f, 22f)
            lineTo(16f, 18f)
            horizontalLineTo(20f)
            curveTo(21.1f, 18f, 22f, 17.1f, 22f, 16f)
            verticalLineTo(4f)
            curveTo(22f, 2.9f, 21.1f, 2f, 20f, 2f)
            close()
            // AI sparkle
            moveTo(12f, 6f)
            lineTo(13f, 9f)
            lineTo(16f, 10f)
            lineTo(13f, 11f)
            lineTo(12f, 14f)
            lineTo(11f, 11f)
            lineTo(8f, 10f)
            lineTo(11f, 9f)
            close()
        }
    }.build()

