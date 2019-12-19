-dontobfuscate

# Keep annotations with RUNTIME retention and their defaults.
-keepattributes RuntimeVisible*Annotations, AnnotationDefault

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# Parcelable CREATOR fields are looked up reflectively when de-parceling.
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

# Kotlin
-keep class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keep class kotlinx.coroutines.Deferred {}
