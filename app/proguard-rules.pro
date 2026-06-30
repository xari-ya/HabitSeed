-keepattributes Signature,*Annotation*

# Firestore maps these DTOs by reflection.
-keep class com.habitseed.app.data.social.dto.** { *; }
