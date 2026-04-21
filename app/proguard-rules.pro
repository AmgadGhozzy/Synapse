############################################
# 🔥 MINIMAL SAFE RULES (MOSHI CODEGEN)
############################################

# Keep annotations metadata (required by Room + Moshi codegen)
-keepattributes *Annotation*

############################################
# 🔥 HILT APPLICATION KEEP (REQUIRED)
############################################

# Keep Application class
-keep class io.synapse.ai.Application { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep @HiltAndroidApp classes
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }

# Keep entry points
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager { *; }

############################################
# 🔥 ENUM SAFETY (CefrLevel)
############################################

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

############################################
# 🔥 FIREBASE CRASHLYTICS FIX
############################################

-dontwarn com.google.firebase.crashlytics.buildtools.reloc.afu.org.checkerframework.checker.formatter.qual.ConversionCategory
-dontwarn com.google.firebase.crashlytics.buildtools.reloc.org.checkerframework.checker.formatter.qual.ConversionCategory

############################################
# 🔥 OPTIONAL (Only if warnings appear)
############################################

-dontwarn org.checkerframework.**