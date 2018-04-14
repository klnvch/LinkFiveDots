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