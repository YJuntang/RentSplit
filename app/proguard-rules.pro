# ProGuard Rules for RentApp

# 1. Room DB & Model rules
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class * extends androidx.room.RoomDatabase
-keep class com.rentsplit.data.model.** { *; }

# 2. Hilt Rules
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# 3. ML Kit Text Recognition
-keep class com.google.mlkit.vision.text.** { *; }

# 4. JSON Serialization Keep Rules (for backups)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.rentsplit.data.model.** { *; }
-keep class com.rentsplit.util.BackupManager$** { *; }
