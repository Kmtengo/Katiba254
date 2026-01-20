package com.katiba.app.ui.constitution

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katiba.app.ui.theme.KatibaColors

/**
 * Preamble screen displaying the Constitution's preamble
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreambleScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Preamble",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
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

            // Preamble content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Title section
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(KatibaColors.BeadGold.copy(alpha = 0.1f))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "📜 PREAMBLE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KatibaColors.BeadGold
                    )
                }

                // Opening statement
                Text(
                    text = "We, the people of Kenya —",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Preamble paragraphs
                PreambleParagraph(
                    keyword = "ACKNOWLEDGING",
                    content = "the supremacy of the Almighty God of all creation:"
                )

                PreambleParagraph(
                    keyword = "HONOURING",
                    content = "those who heroically struggled to bring freedom and justice to our land:"
                )

                PreambleParagraph(
                    keyword = "PROUD",
                    content = "of our ethnic, cultural and religious diversity, and determined to live in peace and unity as one indivisible sovereign nation:"
                )

                PreambleParagraph(
                    keyword = "RESPECTFUL",
                    content = "of the environment, which is our heritage, and determined to sustain it for the benefit of future generations:"
                )

                PreambleParagraph(
                    keyword = "COMMITTED",
                    content = "to nurturing and protecting the well-being of the individual, the family, communities and the nation:"
                )

                PreambleParagraph(
                    keyword = "RECOGNISING",
                    content = "the aspirations of all Kenyans for a government based on the essential values of human rights, equality, freedom, democracy, social justice and the rule of law:"
                )

                PreambleParagraph(
                    keyword = "EXERCISING",
                    content = "our sovereign and inalienable right to determine the form of governance of our country and having participated fully in the making of this Constitution:"
                )

                // Closing statement
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ADOPT, ENACT and give this Constitution to ourselves and to our future generations.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                // God Bless Kenya
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    KatibaColors.KenyaGreen.copy(alpha = 0.1f),
                                    KatibaColors.KenyaRed.copy(alpha = 0.1f),
                                    KatibaColors.KenyaBlack.copy(alpha = 0.1f)
                                )
                            )
                        )
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "GOD BLESS KENYA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        color = KatibaColors.KenyaGreen
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PreambleParagraph(
    keyword: String,
    content: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = keyword,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = KatibaColors.KenyaRed,
            letterSpacing = 2.sp
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 26.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
        )
    }
}

