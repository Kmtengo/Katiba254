package com.katiba.app.data.repository

import com.katiba.app.data.model.*

/**
 * Sample data repository providing static content for UI prototyping.
 * Constitution data is now loaded from ConstitutionRepository.
 */
object SampleDataRepository {

    fun getDailyContent(): DailyContent {
        // Try to get real data from ConstitutionRepository
        if (ConstitutionRepository.isLoaded()) {
            val (chapter, article) = ConstitutionRepository.getRandomArticle()
                ?: return getFallbackDailyContent()

            val clause = article.clauses.firstOrNull() ?: return getFallbackDailyContent()

            return DailyContent(
                id = "daily_${article.number}",
                date = "2026-01-12",
                clause = clause,
                chapterTitle = chapter.title,
                articleTitle = article.title,
                articleNumber = article.number,
                aiDescription = generateAiDescription(article, chapter),
                videoUrl = "",
                videoThumbnailUrl = "",
                educatorName = "Constitutional Expert",
                nextSteps = generateNextSteps(article),
                tips = generateTips(chapter)
            )
        }
        return getFallbackDailyContent()
    }

    private fun getFallbackDailyContent(): DailyContent {
        return DailyContent(
            id = "daily_2026_01_12",
            date = "2026-01-12",
            clause = Clause(
                number = "1",
                text = "Every person has the right to life. The life of a person begins at conception.",
                subClauses = emptyList()
            ),
            chapterTitle = "The Bill of Rights",
            articleTitle = "Right to life",
            articleNumber = 26,
            aiDescription = """
                Article 26 of the Kenyan Constitution protects the most fundamental right of all—the right to life. 
                It states that every person has the right to life and that this life begins at conception.
                
                The Constitution also provides specific circumstances regarding the termination of pregnancy, which can only be done if a trained health professional opinion is that there is need for emergency treatment, or the life or health of the mother is in danger, or if permitted by any other law.
            """.trimIndent(),
            videoUrl = "https://example.com/videos/article26_explained.mp4",
            videoThumbnailUrl = "https://example.com/thumbnails/article26.jpg",
            educatorName = "Hon. Martha Karua",
            nextSteps = listOf(
                "Understand the legal protections for the right to life",
                "Learn about the exceptions provided in the Constitution",
                "Discuss the importance of the Bill of Rights in protecting citizens"
            ),
            tips = listOf(
                "The Bill of Rights is the foundation of our democracy",
                "Every person's life is sacred and protected by law",
                "Know your rights and how to defend them"
            )
        )
    }

    private fun generateAiDescription(article: Article, chapter: Chapter): String {
        val clauseTexts = article.clauses.take(2).joinToString("\n\n") { clause ->
            val subClauseText = if (clause.subClauses.isNotEmpty()) {
                "\n" + clause.subClauses.joinToString("\n") { "(${it.label}) ${it.text}" }
            } else ""
            "Clause ${clause.number}: ${clause.text}$subClauseText"
        }

        return """
            Article ${article.number} - ${article.title}
            
            This article is part of Chapter ${chapter.number}: ${chapter.title}.
            
            $clauseTexts
            
            Understanding this article helps you know your constitutional rights and responsibilities as a Kenyan citizen.
        """.trimIndent()
    }

    private fun generateNextSteps(article: Article): List<String> {
        return listOf(
            "Read the full text of Article ${article.number}",
            "Understand how ${article.title} applies to your daily life",
            "Learn about related articles in the Constitution",
            "Discuss with friends and family about these provisions"
        )
    }

    private fun generateTips(chapter: Chapter): List<String> {
        return listOf(
            "Chapter ${chapter.number} covers: ${chapter.title}",
            "The Constitution is the supreme law of Kenya",
            "Every citizen has the right to know and understand the Constitution"
        )
    }

    fun getLessons(): List<Lesson> {
        return listOf(
            // Preamble & Chapter 1
            Lesson("lesson_1", "We The People", "Understanding sovereignty and who holds power in Kenya", 1, 1, 50, isCompleted = true, isLocked = false),
            Lesson("lesson_2", "The Supreme Law", "Why the Constitution is the highest law", 1, 2, 50, isCompleted = true, isLocked = false),
            Lesson("lesson_3", "Defending Our Constitution", "Every citizen's duty to protect the Constitution", 1, 3, 50, isCurrent = true, isLocked = false),

            // Chapter 2
            Lesson("lesson_4", "Kenya: Our Republic", "The declaration and symbols of our nation", 2, 1, 60, isLocked = false),
            Lesson("lesson_5", "Our Languages", "Kiswahili, English, and cultural languages", 2, 2, 60, isLocked = false),
            Lesson("lesson_6", "Separation of State and Religion", "Understanding secular governance", 2, 3, 60),

            // Chapter 3
            Lesson("lesson_7", "Citizenship by Birth", "Who qualifies as a Kenyan citizen", 3, 1, 70),
            Lesson("lesson_8", "Registration & Dual Citizenship", "Acquiring Kenyan citizenship", 3, 2, 70),

            // Chapter 4 - Bill of Rights
            Lesson("lesson_9", "Your Fundamental Rights", "Overview of the Bill of Rights", 4, 1, 80),
            Lesson("lesson_10", "Right to Life", "The most fundamental human right", 4, 2, 80),
            Lesson("lesson_11", "Equality for All", "Freedom from discrimination", 4, 3, 80),
            Lesson("lesson_12", "Human Dignity", "Inherent worth of every person", 4, 4, 80),
            Lesson("lesson_13", "Freedom & Security", "Protection from arbitrary detention", 4, 5, 80),
            Lesson("lesson_14", "Privacy Rights", "Protection of personal information", 4, 6, 80),
            Lesson("lesson_15", "Freedom of Expression", "Your right to speak and be heard", 4, 7, 80),
            Lesson("lesson_16", "Political Rights", "Voting and political participation", 4, 8, 80),
            Lesson("lesson_17", "Economic & Social Rights", "Health, education, and housing", 4, 9, 80),
            Lesson("lesson_18", "Access to Justice", "Fair hearings and legal representation", 4, 10, 80),

            // Chapter 5
            Lesson("lesson_19", "Land Ownership", "Principles of land in Kenya", 5, 1, 70),
            Lesson("lesson_20", "Environmental Rights", "Clean and healthy environment for all", 5, 2, 70),

            // Chapter 6
            Lesson("lesson_21", "Leaders with Integrity", "Standards for public officers", 6, 1, 80),

            // More chapters...
            Lesson("lesson_22", "How Parliament Works", "The legislative process", 8, 1, 90),
            Lesson("lesson_23", "The Executive Branch", "Presidential powers and cabinet", 9, 1, 90),
            Lesson("lesson_24", "Judicial Independence", "How courts protect your rights", 10, 1, 90),
            Lesson("lesson_25", "County Governments", "Devolution and local governance", 11, 1, 100),
            Lesson("lesson_26", "Public Money", "Principles of public finance", 12, 1, 100),
            Lesson("lesson_27", "Amending the Constitution", "How to change the supreme law", 16, 1, 120)
        )
    }

    fun getUserProfile(): UserProfile {
        return UserProfile(
            id = "user_001",
            name = "Amani Wanjiku",
            email = "amani@example.com",
            county = "Nairobi",
            constituency = "Westlands",
            ward = "Parklands",
            joinedDate = "2024-06-15",
            streak = 7,
            longestStreak = 21,
            totalLessonsCompleted = 15,
            badges = listOf(
                Badge("badge_1", "Constitution Starter", "Completed your first lesson", "", "2024-06-15", true),
                Badge("badge_2", "Rights Champion", "Completed the Bill of Rights chapter", "", "2024-08-20", true),
                Badge("badge_3", "Week Warrior", "Maintained a 7-day streak", "", "2024-12-29", true),
                Badge("badge_4", "Master Scholar", "Complete all lessons", "", null, false),
                Badge("badge_5", "Community Leader", "Share with 10 friends", "", null, false)
            )
        )
    }

    fun getRecentActivity(): List<ActivityRecord> {
        return listOf(
            ActivityRecord("act_1", ActivityType.DAILY_CLAUSE_READ, "Read Clause of the Day", "2024-12-29", 10),
            ActivityRecord("act_2", ActivityType.LESSON_COMPLETED, "Completed: The Supreme Law", "2024-12-28", 50),
            ActivityRecord("act_3", ActivityType.STREAK_MILESTONE, "7-Day Streak Achieved!", "2024-12-28", 100),
            ActivityRecord("act_4", ActivityType.LESSON_COMPLETED, "Completed: We The People", "2024-12-27", 50),
            ActivityRecord("act_5", ActivityType.DAILY_CLAUSE_READ, "Read Clause of the Day", "2024-12-26", 10)
        )
    }
}

