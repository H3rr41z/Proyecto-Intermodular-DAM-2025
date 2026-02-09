# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep data classes for Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.renaix.**$$serializer { *; }
-keepclassmembers class com.renaix.** {
    *** Companion;
}
-keepclasseswithmembers class com.renaix.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep SQLDelight generated code
-keep class com.renaix.data.local.database.** { *; }

# Keep Ktor
-keep class io.ktor.** { *; }
