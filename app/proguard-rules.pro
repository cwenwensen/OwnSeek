# Retrofit / OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes *Annotation*
-keep class com.deepseek.chat.data.api.dto.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }

# Compose
-keep class androidx.compose.** { *; }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.deepseek.chat.**$$serializer { *; }
-keepclassmembers class com.deepseek.chat.** {
    *** Companion;
}
-keepclasseswithmembers class com.deepseek.chat.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# 保护 domain model 类，防止序列化在 Release 版本中因混淆而失败
-keep class com.deepseek.chat.domain.model.** { *; }
