package com.katiba.app.ui.plans

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.katiba.app.data.model.Lesson
import com.katiba.app.data.repository.SampleDataRepository
import com.katiba.app.ui.components.BeadworkPageIndicator
import com.katiba.app.ui.theme.KatibaColors

/**
 * Interactive lesson screen with bite-sized content chunks, quiz questions,
 * progress indicators, and XP/points earned
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    lessonId: String,
    onBackClick: () -> Unit,
    onCompleteLesson: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lessons = remember { SampleDataRepository.getLessons() }
    val lesson = remember(lessonId) { 
        lessons.find { it.id == lessonId } 
    }
    
    var currentPage by remember { mutableIntStateOf(0) }
    val totalPages = 5 // Content + 4 quiz questions
    
    var quizAnswers by remember { mutableStateOf(List(4) { -1 }) }
    val correctAnswers = remember { listOf(0, 2, 1, 3) } // Sample correct answers
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (lesson != null) {
                            Text(
                                text = lesson.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text("Lesson")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
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
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { (currentPage + 1).toFloat() / totalPages },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = KatibaColors.KenyaGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            // Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                when (currentPage) {
                    0 -> LessonContent(lesson = lesson, scrollState = scrollState)
                    else -> QuizQuestion(
                        questionNumber = currentPage,
                        selectedAnswer = quizAnswers[currentPage - 1],
                        onAnswerSelected = { answer ->
                            val newAnswers = quizAnswers.toMutableList()
                            newAnswers[currentPage - 1] = answer
                            quizAnswers = newAnswers
                        }
                    )
                }
            }
            
            // Page indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                BeadworkPageIndicator(
                    pageCount = totalPages,
                    currentPage = currentPage
                )
            }
            
            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button (except on first page)
                if (currentPage > 0) {
                    Button(
                        onClick = { currentPage-- },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.width(120.dp))
                }
                
                // Next/Finish button
                Button(
                    onClick = {
                        if (currentPage < totalPages - 1) {
                            currentPage++
                        } else {
                            // Lesson completed
                            onCompleteLesson()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KatibaColors.KenyaGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.width(120.dp),
                    enabled = currentPage == 0 || quizAnswers[currentPage - 1] >= 0
                ) {
                    Text(if (currentPage < totalPages - 1) "Next" else "Finish")
                }
            }
        }
    }
}

@Composable
private fun LessonContent(
    lesson: Lesson?,
    scrollState: ScrollState
) {
    if (lesson == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Lesson not found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Lesson header
        Text(
            text = "Chapter ${lesson.chapterNumber}",
            style = MaterialTheme.typography.labelMedium,
            color = KatibaColors.KenyaGreen
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = lesson.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = lesson.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sample lesson content
        Text(
            text = "What You'll Learn",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LessonBulletPoint("The key principles in this chapter")
        LessonBulletPoint("How these principles apply to your daily life")
        LessonBulletPoint("Your rights and responsibilities")
        LessonBulletPoint("Historical context and importance")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sample content section
        Text(
            text = "Key Concepts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "This lesson covers fundamental principles established in the Kenyan Constitution. " +
                   "You'll learn about the structure of government, separation of powers, and how " +
                   "these concepts protect citizens' rights and freedoms.\n\n" +
                   "The Constitution establishes checks and balances to prevent abuse of power " +
                   "and ensures that all Kenyans have equal protection under the law.\n\n" +
                   "After completing this lesson, you'll understand how these principles " +
                   "affect your daily life and civic participation.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // XP reward info
        Surface(
            color = KatibaColors.BeadGold.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "⭐",
                    style = MaterialTheme.typography.titleLarge
                )
                Column {
                    Text(
                        text = "Complete this lesson",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Earn ${lesson.xpReward} XP and progress toward your next badge",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonBulletPoint(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(KatibaColors.KenyaGreen)
                .align(Alignment.CenterVertically)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun QuizQuestion(
    questionNumber: Int,
    selectedAnswer: Int,
    onAnswerSelected: (Int) -> Unit
) {
    val questions = listOf(
        "Which article of the Constitution establishes that sovereign power belongs to the people?",
        "What does the Constitution say about State religion in Kenya?",
        "Which languages are recognized as official languages in Kenya?",
        "How many counties are established by the Constitution?"
    )
    
    val options = listOf(
        listOf("Article 1", "Article 2", "Article 3", "Article 4"),
        listOf("Christianity is the State religion", "Islam is the State religion", "There shall be no State religion", "All religions are equally recognized"),
        listOf("English only", "Kiswahili and English", "Kiswahili only", "English, Kiswahili, and all indigenous languages"),
        listOf("37", "42", "47", "50")
    )
    
    val questionIndex = questionNumber - 1
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Question header
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Question ${questionNumber} of 4",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = questions[questionIndex],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // Answer options
        options[questionIndex].forEachIndexed { index, option ->
            AnswerOption(
                text = option,
                isSelected = selectedAnswer == index,
                onClick = { onAnswerSelected(index) }
            )
        }
    }
}

@Composable
private fun AnswerOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) {
            KatibaColors.KenyaGreen.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) {
                KatibaColors.KenyaGreen
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selection indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            KatibaColors.KenyaGreen
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Option text
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
