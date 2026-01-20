package com.katiba.app.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a chapter in the Kenyan Constitution
 */
@Serializable
data class Chapter(
    val number: Int,
    val title: String,
    val articles: List<Article>
)

/**
 * Represents an article within a chapter
 */
@Serializable
data class Article(
    val number: Int,
    val title: String,
    val clauses: List<Clause>
)

/**
 * Represents a clause within an article
 */
@Serializable
data class Clause(
    val number: String,
    val text: String,
    val subClauses: List<SubClause> = emptyList()
)

/**
 * Represents a sub-clause within a clause (e.g., (a), (b), (c))
 */
@Serializable
data class SubClause(
    val label: String,
    val text: String
)

/**
 * Full Constitution structure for YAML loading
 */
@Serializable
data class Constitution(
    val preamble: String = "",
    val chapters: List<Chapter>
)

/**
 * Daily content served to users
 */
@Serializable
data class DailyContent(
    val id: String,
    val date: String, // ISO date format
    val clause: Clause,
    val chapterTitle: String,
    val articleTitle: String,
    val articleNumber: Int,
    val aiDescription: String,
    val videoUrl: String = "",
    val videoThumbnailUrl: String = "",
    val educatorName: String = "",
    val nextSteps: List<String>,
    val tips: List<String>
)

/**
 * User profile data
 */
@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String = "",
    val county: String = "",
    val constituency: String = "",
    val ward: String = "",
    val joinedDate: String,
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val totalLessonsCompleted: Int = 0,
    val xp: Int = 0,
    val badges: List<Badge> = emptyList()
)

/**
 * Achievement badge
 */
@Serializable
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val earnedDate: String? = null,
    val isEarned: Boolean = false
)

/**
 * Learning progress for the Plans feature
 */
@Serializable
data class LearningProgress(
    val userId: String,
    val completedLessons: List<String>,
    val currentLessonId: String,
    val totalXp: Int = 0
)

/**
 * A lesson in the learning path
 */
@Serializable
data class Lesson(
    val id: String,
    val title: String,
    val description: String,
    val chapterNumber: Int,
    val order: Int,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val isLocked: Boolean = true,
    val isCurrent: Boolean = false
)

/**
 * Activity record for profile
 */
@Serializable
data class ActivityRecord(
    val id: String,
    val type: ActivityType,
    val title: String,
    val date: String,
    val xpEarned: Int = 0
)

@Serializable
enum class ActivityType {
    LESSON_COMPLETED,
    BADGE_EARNED,
    STREAK_MILESTONE,
    CHAPTER_COMPLETED,
    DAILY_CLAUSE_READ
}
