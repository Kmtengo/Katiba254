package com.katiba.app.ui.constitution

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.katiba.app.data.model.Chapter
import com.katiba.app.data.repository.ConstitutionRepository
import com.katiba.app.ui.theme.KatibaColors

/**
 * Bible reader-inspired Constitution screen with chapter/article navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstitutionScreen(
    onChapterClick: (Int) -> Unit,
    onPreambleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chapters = remember { ConstitutionRepository.chapters }
    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }
    var showChapterList by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(56.dp),
                title = {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "The Constitution",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "of Kenya, 2010",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Beadwork accent line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                KatibaColors.KenyaBlack,
                                KatibaColors.KenyaRed,
                                KatibaColors.KenyaGreen,
                                KatibaColors.KenyaWhite,
                                KatibaColors.KenyaGreen,
                                KatibaColors.KenyaRed,
                                KatibaColors.KenyaBlack
                            )
                        )
                    )
            )
            
            if (showChapterList) {
                ChapterListView(
                    chapters = chapters,
                    onChapterClick = { chapter ->
                        selectedChapter = chapter
                        onChapterClick(chapter.number)
                    },
                    onPreambleClick = onPreambleClick
                )
            }
        }
    }
}

@Composable
private fun ChapterListView(
    chapters: List<Chapter>,
    onChapterClick: (Chapter) -> Unit,
    onPreambleClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Preamble item
        item {
            ChapterListItem(
                chapterNumber = 0,
                chapterTitle = "Preamble",
                articleCount = 0,
                isPreamble = true,
                onClick = onPreambleClick
            )
        }
        
        // Chapter items
        items(chapters) { chapter ->
            ChapterListItem(
                chapterNumber = chapter.number,
                chapterTitle = chapter.title,
                articleCount = chapter.articles.size,
                isPreamble = false,
                onClick = { onChapterClick(chapter) }
            )
        }
        
        // Schedules item
        item {
            ChapterListItem(
                chapterNumber = 99,
                chapterTitle = "Schedules",
                articleCount = 6,
                isPreamble = false,
                isSchedule = true,
                onClick = { /* Navigate to schedules */ }
            )
        }
    }
}

@Composable
private fun ChapterListItem(
    chapterNumber: Int,
    chapterTitle: String,
    articleCount: Int,
    isPreamble: Boolean,
    isSchedule: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Chapter number badge
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        isPreamble -> KatibaColors.BeadGold.copy(alpha = 0.15f)
                        isSchedule -> KatibaColors.KenyaRed.copy(alpha = 0.1f)
                        else -> KatibaColors.KenyaGreen.copy(alpha = 0.1f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isPreamble) {
                Text(
                    text = "📜",
                    style = MaterialTheme.typography.titleMedium
                )
            } else if (isSchedule) {
                Text(
                    text = "📋",
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Text(
                    text = chapterNumber.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KatibaColors.KenyaGreen
                )
            }
        }
        
        // Title and info
        Column(modifier = Modifier.weight(1f)) {
            if (!isPreamble && !isSchedule) {
                Text(
                    text = "Chapter $chapterNumber",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = chapterTitle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (articleCount > 0) {
                Text(
                    text = "$articleCount articles",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Arrow indicator
        Text(
            text = "→",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
