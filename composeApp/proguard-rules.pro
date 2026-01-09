# =====================================
# Katiba App - ProGuard/R8 Rules
# =====================================
# These rules are used by R8 to optimize the Android APK

# =====================================
# Kotlin Serialization
# =====================================
# Keep serialization classes for navigation and data persistence
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep serializers for @Serializable classes
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
}
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep the generated serializers
-keep,includedescriptorclasses class com.katiba.app.**$$serializer { *; }
-keepclassmembers class com.katiba.app.** {
    *** Companion;
}

# =====================================
# Compose Multiplatform (OPTIMIZED)
# =====================================
# Keep only Compose classes accessed reflectively
# R8 can now remove unused Compose APIs

# Keep Compose runtime stability infrastructure
-keep @androidx.compose.runtime.Stable class * { *; }
-keep @androidx.compose.runtime.Immutable class * { *; }

# Keep Composable functions (accessed via reflection)
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep specific Compose internals that use reflection
-keep class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt { *; }
-keep class androidx.compose.ui.platform.DisposableSaveableStateRegistry_androidKt { *; }

# Don't warn about missing Compose classes (R8 will remove unused ones)
-dontwarn androidx.compose.**

# =====================================
# Kotlin Coroutines
# =====================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}

# =====================================
# Navigation Routes (Type-safe navigation) - OPTIMIZED
# =====================================
# Keep @Serializable routes and their serializers
# But allow R8 to remove unused methods

# Keep generated serializers for routes
-keep class com.katiba.app.ui.navigation.**$$serializer { *; }

# Keep serializable route classes structure
-keepclassmembers @kotlinx.serialization.Serializable class com.katiba.app.ui.navigation.** {
    <init>(...);
    *** Companion;
    kotlinx.serialization.KSerializer serializer();
}

# =====================================
# Data Models - OPTIMIZED
# =====================================
# Keep @Serializable data models and their serializers
# But allow R8 to remove unused fields/methods

# Keep serializable data models
-keep @kotlinx.serialization.Serializable class com.katiba.app.data.model.** { *; }

# Keep generated serializers for models
-keep class com.katiba.app.data.model.**$$serializer { *; }

# Keep serializer() companion methods
-keepclassmembers class com.katiba.app.data.model.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer();
}

# Keep enum values for ActivityType
-keepclassmembers enum com.katiba.app.data.model.ActivityType {
    **[] $VALUES;
    public *;
}

# =====================================
# General Android
# =====================================
# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep R8 from stripping interface information
-keepattributes Signature
-keepattributes Exceptions

# =====================================
# Debugging - Remove for production
# =====================================
# Uncomment to preserve source file names and line numbers for stack traces
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name
-renamesourcefileattribute SourceFile

# =====================================
# Remove Logging in Release
# =====================================
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# =====================================
# Optimization Settings
# =====================================
# Don't obfuscate classes with @Keep annotation
-keep @androidx.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

