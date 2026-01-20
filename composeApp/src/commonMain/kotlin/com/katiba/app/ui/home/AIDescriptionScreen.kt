package com.katiba.app.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.katiba.app.data.repository.SampleDataRepository
import com.katiba.app.ui.theme.KatibaColors
import kotlinx.coroutines.launch

/**
 * Multi-page detail view for AI Description and Video
 * Navigation: Tap left/right sides of screen to switch pages
 * Uses Instagram/story-style progress indicators at the top
 */
@Composable
fun AIDescriptionScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyContent = remember { SampleDataRepository.getDailyContent() }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main content pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> AITextDescriptionPage(
                    description = dailyContent.aiDescription,
                    articleNumber = dailyContent.articleNumber,
                    articleTitle = dailyContent.articleTitle
                )
                1 -> VideoPage(
                    videoUrl = dailyContent.videoUrl,
                    educatorName = dailyContent.educatorName,
                    articleTitle = dailyContent.articleTitle
                )
            }
        }

        // Top bar with story progress indicators
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            // Story-style progress indicators
            StoryProgressIndicator(
                pageCount = 2,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            // Close button row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Optional: Page title
                Text(
                    text = if (pagerState.currentPage == 0) "AI Explanation" else "Video Lesson",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Spacer for symmetry
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        // Left tap zone for previous page
        if (pagerState.currentPage > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .align(Alignment.CenterStart)
                    .clickable {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
            )
        }

        // Right tap zone for next page
        if (pagerState.currentPage < 1) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .align(Alignment.CenterEnd)
                    .clickable {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
            )
        }
    }
}

/**
 * Instagram/Story-style horizontal segmented progress indicator
 */
@Composable
private fun StoryProgressIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(pageCount) { index ->
            val isActive = index <= currentPage
            val animatedWeight by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.7f,
                label = "progressWeight"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(
                        if (isActive) Color.White else Color.Gray.copy(alpha = 0.4f)
                    )
            )
        }
    }
}

@Composable
private fun AITextDescriptionPage(
    description: String,
    articleNumber: Int,
    articleTitle: String
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 120.dp) // Account for top bar
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        // AI badge
        Surface(
            color = KatibaColors.BeadGold.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "✨",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "AI-Powered Explanation",
                    style = MaterialTheme.typography.labelMedium,
                    color = KatibaColors.BeadBrown,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Article reference
        Text(
            text = "Article $articleNumber",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = articleTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Beadwork accent line
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            KatibaColors.KenyaBlack,
                            KatibaColors.KenyaRed,
                            KatibaColors.KenyaGreen
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // AI description text
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.6f
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun VideoPage(
    videoUrl: String,
    educatorName: String,
    articleTitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 120.dp) // Account for top bar
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        // Video player placeholder (will be replaced with actual video player)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f) // Vertical reel format
                .clip(RoundedCornerShape(16.dp))
                .background(KatibaColors.KenyaBlack),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Play button
                Surface(
                    color = KatibaColors.KenyaRed,
                    shape = RoundedCornerShape(50)
                ) {
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Video Lesson",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = articleTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Educator info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(KatibaColors.KenyaGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = educatorName.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text(
                    text = educatorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Civic Educator",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
