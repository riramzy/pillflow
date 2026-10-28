# ====================================================================
# PillFlow Production R8 / ProGuard Rules
# ====================================================================

# 1. Preserve Firestore DTOs and reflection / serialization fields
-keep class com.riramzy.pillfllow.data.remote.dto.** { *; }
-keepclassmembers class com.riramzy.pillfllow.data.remote.dto.** { *; }

# 2. Preserve Room SQLite Entities
-keep class com.riramzy.pillfllow.data.local.entity.** { *; }
-keepclassmembers class com.riramzy.pillfllow.data.local.entity.** { *; }

# 3. Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class **$$serializer {
    public static final **$$serializer INSTANCE;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    <fields>;
    <methods>;
}

# 4. Koin DI reflection safety
-dontwarn org.koin.**