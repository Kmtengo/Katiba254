package com.katiba.app.ui.constitution

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.katiba.app.data.model.Article
import com.katiba.app.data.model.Chapter
import com.katiba.app.data.repository.ConstitutionRepository
import com.katiba.app.ui.theme.KatibaColors

/**
 * Clause grid view for navigating articles within a chapter (like Bible verse grid)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClauseGridScreen(
    chapterNumber: Int,
    onBackClick: () -> Unit,
    onArticleClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val chapters = remember { ConstitutionRepository.chapters }
    val chapter = remember(chapterNumber) {
        chapters.find { it.number == chapterNumber } 
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chapter $chapterNumber") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Chapter header
            if (chapter != null) {
                ChapterHeader(chapter = chapter)
            }
            
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Article grid
            if (chapter != null) {
                ArticleGrid(
                    chapter = chapter,
                    onArticleClick = { articleNumber ->
                        onArticleClick(chapterNumber, articleNumber)
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chapter not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterHeader(chapter: Chapter) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Chapter ${chapter.number}",
            style = MaterialTheme.typography.labelMedium,
            color = KatibaColors.KenyaGreen
        )
        Text(
            text = chapter.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "${chapter.articles.size} articles",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ArticleGrid(
    chapter: Chapter,
    onArticleClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Intro item
        item {
            ArticleGridItem(
                text = "Intro",
                isIntro = true,
                onClick = { /* Navigate to chapter intro */ }
            )
        }
        
        // Article number items
        items(chapter.articles) { article ->
            ArticleGridItem(
                text = article.number.toString(),
                isIntro = false,
                onClick = { onArticleClick(article.number) }
            )
        }
    }
}

@Composable
private fun ArticleGridItem(
    text: String,
    isIntro: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isIntro) KatibaColors.KenyaGreen.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = if (isIntro) {
                MaterialTheme.typography.labelMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            fontWeight = if (isIntro) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isIntro) KatibaColors.KenyaGreen 
                   else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
