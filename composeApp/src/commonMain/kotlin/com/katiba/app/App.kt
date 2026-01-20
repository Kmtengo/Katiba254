package com.katiba.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.katiba.app.data.repository.ConstitutionRepository
import com.katiba.app.ui.constitution.ClauseGridScreen
import com.katiba.app.ui.constitution.ConstitutionScreen
import com.katiba.app.ui.constitution.PreambleScreen
import com.katiba.app.ui.constitution.ReadingScreen
import com.katiba.app.ui.home.*
import com.katiba.app.ui.navigation.*
import com.katiba.app.ui.plans.LessonScreen
import com.katiba.app.ui.plans.PlansScreen
import com.katiba.app.ui.profile.ProfileScreen
import com.katiba.app.ui.profile.SettingsScreen
import com.katiba.app.ui.theme.KatibaTheme
import katiba.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    // Load constitution data on first composition
    var isConstitutionLoaded by remember { mutableStateOf(ConstitutionRepository.isLoaded()) }

    LaunchedEffect(Unit) {
        if (!ConstitutionRepository.isLoaded()) {
            withContext(Dispatchers.Default) {
                try {
                    val jsonBytes = Res.readBytes("files/constitution.json")
                    val jsonString = jsonBytes.decodeToString()
                    ConstitutionRepository.loadFromJson(jsonString)
                    isConstitutionLoaded = true
                } catch (e: Exception) {
                    // Log error but continue - app can work with sample data
                    println("Failed to load constitution: ${e.message}")
                    isConstitutionLoaded = true // Continue anyway
                }
            }
        }
    }

    KatibaTheme {
        if (!isConstitutionLoaded) {
            // Show loading indicator while constitution data loads
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
        // Navigation 3 Style: You own the backstack as a mutable list
        val backStack = rememberNavBackStack(HomeRoute)

        // Helper to get current route from backstack
        val currentKey = backStack.lastOrNull()

        // Handle system back button/swipe navigation
        BackHandler(enabled = backStack.size > 1) {
            backStack.removeLast()
        }

        // Determine current tab based on the current key
        val currentTab = remember(currentKey) {
            when (currentKey) {
                is HomeRoute,
                is ClauseDetailRoute,
                is AIDescriptionRoute,
                is TipsRoute,
                is MzalendoRoute -> BottomNavTab.HOME

                is ConstitutionRoute,
                is ChapterListRoute,
                is ClauseGridRoute,
                is ReadingRoute -> BottomNavTab.KATIBA

                is PlansRoute,
                is LessonRoute -> BottomNavTab.PLANS

                is ProfileRoute,
                is SettingsRoute,
                is NotificationsRoute -> BottomNavTab.PROFILE

                else -> BottomNavTab.HOME
            }
        }

        // Determine if bottom bar should be shown
        val showBottomBar = remember(currentKey) {
            currentKey is HomeRoute ||
            currentKey is ConstitutionRoute ||
            currentKey is PlansRoute ||
            currentKey is ProfileRoute ||
            currentKey is MzalendoRoute
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    KatibaBottomNavigation(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            // Navigation 3 Style: Clear back to root and add new tab
                            while (backStack.size > 1) {
                                backStack.removeLast()
                            }
                            // If current root isn't the selected tab, replace it
                            if (backStack.lastOrNull() != tab.route) {
                                if (backStack.isNotEmpty()) {
                                    backStack.removeLast()
                                }
                                backStack.add(tab.route)
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            // Navigation 3 Style: NavDisplay replaces NavHost
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.padding(paddingValues),
                entryDecorators = listOf(
                    rememberSavedStateNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    // Home Tab
                    entry<HomeRoute> {
                        HomeScreen(
                            onClauseCardClick = {
                                backStack.add(ClauseDetailRoute("today"))
                            },
                            onAIDescriptionCardClick = {
                                backStack.add(AIDescriptionRoute("today"))
                            },
                            onTipsCardClick = {
                                backStack.add(TipsRoute("today"))
                            },
                            onNotificationsClick = {
                                backStack.add(NotificationsRoute)
                            },
                            onMzalendoClick = {
                                backStack.add(MzalendoRoute)
                            },
                            onResumeLesson = { lessonId ->
                                backStack.add(LessonRoute(lessonId))
                            }
                        )
                    }

                    entry<MzalendoRoute> {
                        MzalendoScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    entry<NotificationsRoute> {
                        NotificationsScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    entry<ClauseDetailRoute> { key ->
                        ClauseDetailScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    entry<AIDescriptionRoute> { key ->
                        AIDescriptionScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    entry<TipsRoute> { key ->
                        TipsScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    // Constitution Tab
                    entry<ConstitutionRoute> {
                        ConstitutionScreen(
                            onChapterClick = { chapterNumber ->
                                backStack.add(ClauseGridRoute(chapterNumber))
                            },
                            onPreambleClick = {
                                backStack.add(PreambleRoute)
                            }
                        )
                    }

                    entry<PreambleRoute> {
                        PreambleScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }

                    entry<ClauseGridRoute> { key ->
                        ClauseGridScreen(
                            chapterNumber = key.chapterNumber,
                            onBackClick = { backStack.removeLast() },
                            onArticleClick = { chapterNum, articleNum ->
                                backStack.add(ReadingRoute(chapterNum, articleNum))
                            }
                        )
                    }

                    entry<ReadingRoute> { key ->
                        ReadingScreen(
                            chapterNumber = key.chapterNumber,
                            articleNumber = key.articleNumber,
                            onBackClick = { backStack.removeLast() },
                            onNavigateToArticle = { chapterNum, articleNum ->
                                // Replace current reading route with new one
                                backStack.removeLast()
                                backStack.add(ReadingRoute(chapterNum, articleNum))
                            }
                        )
                    }

                    // Plans Tab
                    entry<PlansRoute> {
                        PlansScreen(
                            onLessonClick = { lessonId ->
                                backStack.add(LessonRoute(lessonId))
                            }
                        )
                    }

                    entry<LessonRoute> { key ->
                        LessonScreen(
                            lessonId = key.lessonId,
                            onBackClick = { backStack.removeLast() },
                            onCompleteLesson = { backStack.removeLast() }
                        )
                    }

                    // Profile Tab
                    entry<ProfileRoute> {
                        ProfileScreen(
                            onSettingsClick = {
                                backStack.add(SettingsRoute)
                            }
                        )
                    }

                    entry<SettingsRoute> {
                        SettingsScreen(
                            onBackClick = { backStack.removeLast() }
                        )
                    }
                }
            )
        }
}
