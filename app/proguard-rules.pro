# Event Bus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Firebase Realtime Database
-keepattributes Signature
-keepclassmembers class by.klnvch.link5dots.models.** {
  *;
}

# Guava
-dontwarn java.lang.ClassValue
-dontwarn afu.org.checkerframework.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.checkerframework.**

# Retrofit
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase