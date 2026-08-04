# Project-specific R8 rules belong here. AndroidX, Room, WorkManager, Hilt, and
# Firebase publish the consumer rules required by their libraries, so this app
# does not currently need broad package-level keep rules.

# Retain line mappings for Crashlytics while replacing original Kotlin source
# filenames in the APK. The Crashlytics plugin uploads the release mapping file.
-keepattributes LineNumberTable
-renamesourcefileattribute SourceFile

# Move obfuscated app classes into a flattened package. Library consumer rules
# continue to preserve classes that must retain names for reflection.
-repackageclasses
-allowaccessmodification
