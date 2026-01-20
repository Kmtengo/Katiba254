package com.katiba.app.ui.plans

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katiba.app.data.model.Lesson
import com.katiba.app.data.repository.SampleDataRepository
import com.katiba.app.ui.theme.KatibaColors

/**
 * Duolingo-inspired learning path screen with a winding path layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lessons = remember { SampleDataRepository.getLessons() }
    val userProfile = remember { SampleDataRepository.getUserProfile() }

    // Group lessons by chapter for milestone markers
    val lessonsByChapter = remember(lessons) {
        lessons.groupBy { it.chapterNumber }
    }

    // Create a flat list with chapter markers inserted
    val pathItems = remember(lessons, lessonsByChapter) {
        buildPathItems(lessons, lessonsByChapter)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Learning Path",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        // XP indicator
                        XpBadge(xp = userProfile.xp)
                        Spacer(modifier = Modifier.width(8.dp))
                        // Streak indicator
                        StreakBadge(streak = userProfile.streak)
                        Spacer(modifier = Modifier.width(8.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                HorizontalDivider(thickness = 2.dp, color = Color.Gray.copy(alpha = 0.3f))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Progress summary
            ProgressSummaryCard(
                completedLessons = lessons.count { it.isCompleted },
                totalLessons = lessons.size,
                currentChapter = lessonsByChapter.entries
                    .find { it.value.any { lesson -> lesson.isCurrent } }
                    ?.key ?: 1
            )

            // Winding learning path
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(
                    items = pathItems,
                    key = { _, item -> item.key }
                ) { index, item ->
                    when (item) {
                        is PathItem.ChapterMilestone -> {
                            ChapterMilestoneNode(
                                chapterNumber = item.chapterNumber,
                                chapterTitle = item.title,
                                isCompleted = item.isCompleted,
                                isLocked = item.isLocked
                            )
                        }
                        is PathItem.LessonItem -> {
                            val position = calculateNodePosition(item.indexInPath)
                            val nextItem = pathItems.getOrNull(index + 1)
                            val showConnector = nextItem != null && nextItem !is PathItem.ChapterMilestone

                            WindingLessonNode(
                                lesson = item.lesson,
                                horizontalPosition = position,
                                showConnector = showConnector,
                                nextPosition = if (showConnector) {
                                    calculateNodePosition(item.indexInPath + 1)
                                } else NodePosition.CENTER,
                                onClick = {
                                    if (!item.lesson.isLocked) {
                                        onLessonClick(item.lesson.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Represents the horizontal position of a node in the winding path
 */
enum class NodePosition {
    LEFT, CENTER, RIGHT
}

/**
 * Calculate the position of a node based on its index to create a winding pattern
 * Pattern: CENTER -> RIGHT -> CENTER -> LEFT -> CENTER -> RIGHT -> ...
 */
private fun calculateNodePosition(index: Int): NodePosition {
    return when (index % 4) {
        0 -> NodePosition.CENTER
        1 -> NodePosition.RIGHT
        2 -> NodePosition.CENTER
        3 -> NodePosition.LEFT
        else -> NodePosition.CENTER
    }
}

/**
 * Sealed class representing items in the learning path
 */
sealed class PathItem(val key: String) {
    data class ChapterMilestone(
        val chapterNumber: Int,
        val title: String,
        val isCompleted: Boolean,
        val isLocked: Boolean
    ) : PathItem("chapter_$chapterNumber")

    data class LessonItem(
        val lesson: Lesson,
        val indexInPath: Int
    ) : PathItem("lesson_${lesson.id}")
}

/**
 * Build the list of path items with chapter milestones inserted
 */
private fun buildPathItems(
    lessons: List<Lesson>,
    lessonsByChapter: Map<Int, List<Lesson>>
): List<PathItem> {
    val items = mutableListOf<PathItem>()
    var pathIndex = 0

    lessonsByChapter.toSortedMap().forEach { (chapterNumber, chapterLessons) ->
        // Add chapter milestone
        val chapterTitle = getChapterTitle(chapterNumber)
        items.add(
            PathItem.ChapterMilestone(
                chapterNumber = chapterNumber,
                title = chapterTitle,
                isCompleted = chapterLessons.all { it.isCompleted },
                isLocked = chapterLessons.all { it.isLocked }
            )
        )

        // Add lessons
        chapterLessons.forEach { lesson ->
            items.add(PathItem.LessonItem(lesson = lesson, indexInPath = pathIndex))
            pathIndex++
        }
    }

    return items
}

private fun getChapterTitle(chapterNumber: Int): String {
    return when (chapterNumber) {
        1 -> "The Preamble"
        2 -> "The Republic"
        3 -> "Citizenship"
        4 -> "Bill of Rights"
        5 -> "Land & Environment"
        6 -> "Leadership"
        7 -> "Legislature"
        8 -> "Executive"
        9 -> "Judiciary"
        10 -> "Devolution"
        11 -> "Public Finance"
        12 -> "Public Service"
        else -> "Chapter $chapterNumber"
    }
}

@Composable
private fun XpBadge(xp: Int) {
    Surface(
        color = KatibaColors.BeadGold.copy(alpha = 0.15f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "⭐",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = xp.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = KatibaColors.BeadGold
            )
        }
    }
}

@Composable
private fun StreakBadge(streak: Int) {
    Surface(
        color = KatibaColors.KenyaRed.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "\uD83D\uDD25",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = streak.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = KatibaColors.KenyaRed
            )
        }
    }
}

@Composable
private fun ProgressSummaryCard(
    completedLessons: Int,
    totalLessons: Int,
    currentChapter: Int
) {
    val progress = if (totalLessons > 0) completedLessons.toFloat() / totalLessons else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        KatibaColors.KenyaGreen,
                        KatibaColors.DarkGreen
                    )
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Your Journey",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$completedLessons of $totalLessons lessons",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Chapter $currentChapter",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // Circular progress percentage
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChapterMilestoneNode(
    chapterNumber: Int,
    chapterTitle: String,
    isCompleted: Boolean,
    isLocked: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Milestone shield/badge
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = if (isCompleted) 12.dp else 4.dp,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(
                    brush = when {
                        isCompleted -> Brush.verticalGradient(
                            colors = listOf(KatibaColors.BeadGold, KatibaColors.BeadOrange)
                        )
                        isLocked -> Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            )
                        )
                        else -> Brush.verticalGradient(
                            colors = listOf(KatibaColors.KenyaGreen, KatibaColors.DarkGreen)
                        )
                    }
                )
                .then(
                    if (!isLocked && !isCompleted) {
                        Modifier.border(3.dp, KatibaColors.BeadGold, CircleShape)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Text(
                    text = "\uD83C\uDFC6",
                    fontSize = 32.sp
                )
            } else if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Text(
                    text = "\uD83D\uDCDA",
                    fontSize = 32.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Chapter $chapterNumber",
            style = MaterialTheme.typography.labelMedium,
            color = if (isLocked) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            } else {
                KatibaColors.KenyaGreen
            },
            fontWeight = FontWeight.Bold
        )

        Text(
            text = chapterTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isLocked) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.onBackground
            },
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun WindingLessonNode(
    lesson: Lesson,
    horizontalPosition: NodePosition,
    showConnector: Boolean,
    nextPosition: NodePosition,
    onClick: () -> Unit
) {
    val nodeSize = if (lesson.isCurrent) 72.dp else 64.dp

    // Calculate horizontal offset based on position
    val horizontalOffset = when (horizontalPosition) {
        NodePosition.LEFT -> (-80).dp
        NodePosition.CENTER -> 0.dp
        NodePosition.RIGHT -> 80.dp
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (showConnector) 120.dp else 80.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Draw connector path to next node
            if (showConnector) {
                ConnectorPath(
                    fromPosition = horizontalPosition,
                    toPosition = nextPosition,
                    isCompleted = lesson.isCompleted,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Lesson node
            Column(
                modifier = Modifier.offset(x = horizontalOffset),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main circular button
                Box(
                    modifier = Modifier
                        .size(nodeSize)
                        .shadow(
                            elevation = when {
                                lesson.isCurrent -> 16.dp
                                lesson.isCompleted -> 8.dp
                                else -> 4.dp
                            },
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .background(
                            brush = when {
                                lesson.isCompleted -> Brush.verticalGradient(
                                    colors = listOf(KatibaColors.KenyaGreen, KatibaColors.DarkGreen)
                                )
                                lesson.isCurrent -> Brush.verticalGradient(
                                    colors = listOf(KatibaColors.BeadGold, KatibaColors.BeadOrange)
                                )
                                lesson.isLocked -> Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                    )
                                )
                                else -> Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        )
                        .then(
                            if (lesson.isCurrent) {
                                Modifier.border(4.dp, Color.White, CircleShape)
                            } else if (!lesson.isLocked && !lesson.isCompleted) {
                                Modifier.border(3.dp, KatibaColors.KenyaGreen.copy(alpha = 0.5f), CircleShape)
                            } else Modifier
                        )
                        .clickable(enabled = !lesson.isLocked, onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        lesson.isCompleted -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        lesson.isCurrent -> {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Current",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        lesson.isLocked -> {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        else -> {
                            // Upcoming unlocked lesson
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(KatibaColors.KenyaGreen)
                            )
                        }
                    }
                }

                // Lesson title below node
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (lesson.isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        lesson.isLocked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        lesson.isCurrent -> KatibaColors.BeadGold
                        else -> MaterialTheme.colorScheme.onBackground
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.widthIn(max = 120.dp)
                )

                // XP reward badge for current lesson
                if (lesson.isCurrent) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = KatibaColors.BeadGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "+${lesson.xpReward} XP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = KatibaColors.BeadOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectorPath(
    fromPosition: NodePosition,
    toPosition: NodePosition,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val pathColor = if (isCompleted) {
        KatibaColors.KenyaGreen.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Canvas(modifier = modifier) {
        val startX = when (fromPosition) {
            NodePosition.LEFT -> size.width / 2 - 80.dp.toPx()
            NodePosition.CENTER -> size.width / 2
            NodePosition.RIGHT -> size.width / 2 + 80.dp.toPx()
        }

        val endX = when (toPosition) {
            NodePosition.LEFT -> size.width / 2 - 80.dp.toPx()
            NodePosition.CENTER -> size.width / 2
            NodePosition.RIGHT -> size.width / 2 + 80.dp.toPx()
        }

        val startY = 70.dp.toPx() // Below the node
        val endY = size.height

        // Draw curved path
        val path = Path().apply {
            moveTo(startX, startY)

            // Control points for cubic bezier
            val controlY1 = startY + (endY - startY) * 0.3f
            val controlY2 = startY + (endY - startY) * 0.7f

            cubicTo(
                x1 = startX,
                y1 = controlY1,
                x2 = endX,
                y2 = controlY2,
                x3 = endX,
                y3 = endY
            )
        }

        // Draw path with dashed or solid style
        drawPath(
            path = path,
            color = pathColor,
            style = Stroke(
                width = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}
