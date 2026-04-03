# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ── Stack trace readability ───────────────────────────────────────────────────
# Preserve source file names and line numbers in crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── kotlinx.serialization ─────────────────────────────────────────────────────
# The Kotlin serialization plugin generates serializers at compile time, but
# R8 needs to know to keep them and the annotated data classes.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }

-keep,includedescriptorclasses class com.ikhwan.mandarinkids.**$$serializer { *; }
-keepclassmembers class com.ikhwan.mandarinkids.** {
    *** Companion;
}
-keepclasseswithmembers class com.ikhwan.mandarinkids.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all @Serializable data classes (scenario models, milestone conditions, etc.)
-keep @kotlinx.serialization.Serializable class com.ikhwan.mandarinkids.** { *; }

# ── Room ──────────────────────────────────────────────────────────────────────
# Room ships its own consumer rules, but these are a safety net.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# ── Kotlin & Coroutines ───────────────────────────────────────────────────────
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── Android TTS / Speech ─────────────────────────────────────────────────────
-keep class android.speech.** { *; }
-keep class android.speech.tts.** { *; }