package com.katiba.app.ui.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Navigation 3 Style destinations using NavKey interface
 * All destinations must be @Serializable for state preservation
 */

// Main bottom navigation destinations
@Serializable
data object HomeRoute : NavKey

@Serializable
data object ConstitutionRoute : NavKey

@Serializable
data object PlansRoute : NavKey

@Serializable
data object ProfileRoute : NavKey

// Home detail screens
@Serializable
data class ClauseDetailRoute(val clauseId: String) : NavKey

@Serializable
data class AIDescriptionRoute(val clauseId: String) : NavKey

@Serializable
data class TipsRoute(val clauseId: String) : NavKey

// Constitution reader screens
@Serializable
data object PreambleRoute : NavKey

@Serializable
data class ChapterListRoute(val searchQuery: String = "") : NavKey

@Serializable
data class ClauseGridRoute(val chapterNumber: Int) : NavKey

@Serializable
data class ReadingRoute(val chapterNumber: Int, val articleNumber: Int) : NavKey

// Plans screens
@Serializable
data class LessonRoute(val lessonId: String) : NavKey

// Profile screens
@Serializable
data object SettingsRoute : NavKey

@Serializable
data object NotificationsRoute : NavKey

@Serializable
data object MzalendoRoute : NavKey

/**
 * SerializersModule for KMP state preservation
 * Required for iOS/Native targets where reflection is not available
 */
val navKeySerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(HomeRoute::class, HomeRoute.serializer())
        subclass(ConstitutionRoute::class, ConstitutionRoute.serializer())
        subclass(PlansRoute::class, PlansRoute.serializer())
        subclass(ProfileRoute::class, ProfileRoute.serializer())
        subclass(ClauseDetailRoute::class, ClauseDetailRoute.serializer())
        subclass(AIDescriptionRoute::class, AIDescriptionRoute.serializer())
        subclass(TipsRoute::class, TipsRoute.serializer())
        subclass(PreambleRoute::class, PreambleRoute.serializer())
        subclass(ChapterListRoute::class, ChapterListRoute.serializer())
        subclass(ClauseGridRoute::class, ClauseGridRoute.serializer())
        subclass(ReadingRoute::class, ReadingRoute.serializer())
        subclass(LessonRoute::class, LessonRoute.serializer())
        subclass(SettingsRoute::class, SettingsRoute.serializer())
        subclass(NotificationsRoute::class, NotificationsRoute.serializer())
        subclass(MzalendoRoute::class, MzalendoRoute.serializer())
    }
}
