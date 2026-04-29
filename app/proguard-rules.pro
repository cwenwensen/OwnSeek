# Retrofit / OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.deepseek.chat.data.api.dto.** { *; }

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
