package com.katiba.app.ui.constitution

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.katiba.app.data.model.Article
import com.katiba.app.data.model.Chapter
import com.katiba.app.data.model.Clause
import com.katiba.app.data.repository.ConstitutionRepository
import com.katiba.app.ui.theme.KatibaColors

/**
 * Reading view for constitution content with vertical scroll
 * Similar to Bible reader with verse numbers and chapter navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    chapterNumber: Int,
    articleNumber: Int,
    onBackClick: () -> Unit,
    onNavigateToArticle: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val chapters = remember { ConstitutionRepository.chapters }
    val chapter = remember(chapterNumber) {
        chapters.find { it.number == chapterNumber }
    }
    val article = remember(chapterNumber, articleNumber) {
        chapter?.articles?.find { it.number == articleNumber }
    }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            ReadingTopBar(
                onMenuClick = onBackClick,
                onSearchClick = { /* Search */ }
            )
        },
        bottomBar = {
            if (chapter != null && article != null) {
                ReadingBottomBar(
                    chapter = chapter,
                    article = article,
                    onPreviousClick = {
                        val prevArticle = chapter.articles.find { it.number == articleNumber - 1 }
                        if (prevArticle != null) {
                            onNavigateToArticle(chapterNumber, prevArticle.number)
                        }
                    },
                    onNextClick = {
                        val nextArticle = chapter.articles.find { it.number == articleNumber + 1 }
                        if (nextArticle != null) {
                            onNavigateToArticle(chapterNumber, nextArticle.number)
                        }
                    },
                    hasPrevious = chapter.articles.any { it.number == articleNumber - 1 },
                    hasNext = chapter.articles.any { it.number == articleNumber + 1 }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (chapter != null && article != null) {
                // Article header
                ArticleHeader(
                    chapterTitle = chapter.title,
                    articleNumber = article.number
                )
                
                // Reading content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Article title
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Clauses
                    article.clauses.forEach { clause ->
                        ClauseContent(clause = clause)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Article not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadingTopBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Column {
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu"
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Audio */ }) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Listen"
                    )
                }
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
                // Version/Translation selector placeholder
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "2010",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
        HorizontalDivider(thickness = 2.dp, color = Color.Gray.copy(alpha = 0.3f))
    }
}

@Composable
private fun ArticleHeader(
    chapterTitle: String,
    articleNumber: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = chapterTitle.split(" - ").lastOrNull() ?: chapterTitle,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = articleNumber.toString(),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ClauseContent(clause: Clause) {
    val annotatedText = buildAnnotatedString {
        // Clause number as superscript-style
        if (clause.number.isNotEmpty()) {
            withStyle(
                style = SpanStyle(
                    color = KatibaColors.KenyaGreen,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("(${clause.number}) ")
            }
        }
        
        // Clause text
        withStyle(
            style = SpanStyle(
                color = Color.Unspecified
            )
        ) {
            append(clause.text)
        }
    }
    
    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f
    )
    
    // Sub-clauses
    if (clause.subClauses.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        clause.subClauses.forEachIndexed { index, subClause ->
            Row(
                modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "(${('a' + index)})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KatibaColors.KenyaGreen,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(32.dp)
                )
                Text(
                    text = subClause.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4f
                )
            }
        }
    }
}

@Composable
private fun ReadingBottomBar(
    chapter: Chapter,
    article: Article,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    hasPrevious: Boolean,
    hasNext: Boolean
) {
    Column {
        // Beadwork divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
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
        
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Audio button
                IconButton(onClick = { /* Play audio */ }) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Navigation controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onPreviousClick,
                        enabled = hasPrevious
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous",
                            tint = if (hasPrevious) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            }
                        )
                    }
                    
                    Text(
                        text = "Article ${article.number}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    IconButton(
                        onClick = onNextClick,
                        enabled = hasNext
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next",
                            tint = if (hasNext) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            }
                        )
                    }
                }
                
                // Placeholder for balance
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    }
}
