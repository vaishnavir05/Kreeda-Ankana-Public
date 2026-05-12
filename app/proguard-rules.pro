# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Room entities
-keep class com.example.kreedaankana.data.db.entity.** { *; }
# Keep Firebase
-keep class com.google.firebase.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
