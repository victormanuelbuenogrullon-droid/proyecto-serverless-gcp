# Proguard rules for retrofit, gson, coroutines
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.victorbueno.app.data.models.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
