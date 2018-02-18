# Event Bus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Firebase Realtime Database
-keepattributes Signature
-keepclassmembers by.klnvch.link5dots.models.** {
  *;
}