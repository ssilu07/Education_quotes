# =============================================
# ProGuard Rules for Vocab Tricks Reels
# =============================================

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- Gson ----
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Keep model classes used with Gson
-keep class com.royal.edunotes._models.** { *; }
-keep class com.royal.edunotes._database.ModelDatabase { *; }

# ---- Picasso ----
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**

# ---- Lottie ----
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }

# ---- Google Play Services / AdMob ----
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.**

# ---- Google GenAI (Gemini) ----
-keep class com.google.genai.** { *; }
-dontwarn com.google.genai.**
-keep class com.google.ai.** { *; }
-dontwarn com.google.ai.**

# ---- Jackson (used by GenAI) ----
-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**

# ---- SQLiteAssetHelper ----
-keep class com.readystatesoftware.sqliteasset.** { *; }

# ---- AndroidX ----
-keep class androidx.** { *; }
-dontwarn androidx.**

# ---- Material Design ----
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ---- FileProvider ----
-keep class androidx.core.content.FileProvider { *; }

# ---- SplashScreen ----
-keep class androidx.core.splashscreen.** { *; }

# ---- App classes ----
-keep class com.royal.edunotes._activities.** { *; }
-keep class com.royal.edunotes._fragments.** { *; }
-keep class com.royal.edunotes._adapters.** { *; }

# ---- NavigationTabBar ----
-keep class devlight.io.library.** { *; }
-dontwarn devlight.io.library.**

# ---- Material Tap Target Prompt ----
-keep class uk.co.samuelwall.materialtaptargetprompt.** { *; }

# ---- Apache HTTP Legacy ----
-dontwarn org.apache.http.**
-dontwarn android.net.http.**

# ---- Conscrypt (optional TLS provider used by OkHttp/networking libs) ----
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.OpenSSLProvider

# ---- General ----
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
