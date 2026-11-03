#!/usr/bin/env bash
# Builds LiquidTubeX.apk WITH NewPipeExtractor inside, without Gradle.
#
# Why this exists: build.sh compiles only app/java with a bare javac classpath, so
# the extractor-backed classes (app/java-extractor) never make it into the APK and
# the JS bridge is never attached. Gradle would do this properly, but it needs AGP
# and JitPack; this script needs neither.
#
# Needs: JDK 11+, Android SDK build-tools 36 (for a d8 that can dex Rhino 1.8),
#        platform android-34, zip, and:
#   NPE_JAR   a NewPipeExtractor jar built from the v0.26.5 source
#   NPE_LIBS  directory holding jsoup, rhino, rhino-engine, protobuf-javalite jars
#
# Note: minSdk is 26 here. The extractor uses java.time, and core library
# desugaring is a Gradle/AGP feature, so API 21-25 devices are not supported by
# this build. The Gradle build (./gradlew :app:assembleRelease) keeps minSdk 21.
set -euo pipefail

: "${JAVA_HOME:?set JAVA_HOME}"
: "${ANDROID_HOME:?set ANDROID_HOME}"
: "${NPE_JAR:?set NPE_JAR to the NewPipeExtractor jar}"
: "${NPE_LIBS:?set NPE_LIBS to the directory with the extractor dependency jars}"

BT="${ANDROID_HOME}/build-tools/36.0.0"
PLATFORM="${ANDROID_HOME}/platforms/android-34/android.jar"
APP="$(cd "$(dirname "$0")" && pwd)/app"
OUT="$(pwd)/build"
CP="${PLATFORM}:${NPE_JAR}:${NPE_LIBS}/jsoup-1.22.2.jar:${NPE_LIBS}/rhino-1.8.1.jar:${NPE_LIBS}/rhino-engine-1.8.1.jar:${NPE_LIBS}/protobuf-javalite-4.35.1.jar"

VCODE="$(sed -n 's/.*android:versionCode="\([0-9]*\)".*/\1/p' "$APP/AndroidManifest.xml")"
VNAME="$(sed -n 's/.*android:versionName="\([^"]*\)".*/\1/p' "$APP/AndroidManifest.xml")"
KS="${KS:-$HOME/.liquidtube/liquidtube.jks}"
KSPASS="${KSPASS:-liquidtube}"

rm -rf "$OUT"; mkdir -p "$OUT/gen" "$OUT/classes" "$OUT/dex"

echo "== 1/6 resources"
"$BT/aapt2" compile --dir "$APP/res" -o "$OUT/res.zip"

echo "== 2/6 linking manifest, resources and assets"
"$BT/aapt2" link -o "$OUT/base.apk" -I "$PLATFORM" \
  --manifest "$APP/AndroidManifest.xml" -R "$OUT/res.zip" --java "$OUT/gen" \
  -A "$APP/assets" --min-sdk-version 26 --target-sdk-version 34 \
  --version-code "$VCODE" --version-name "$VNAME" --auto-add-overlay

echo "== 3/6 compiling java (app + extractor sources)"
find "$APP/java" "$APP/java-extractor" "$OUT/gen" -name '*.java' > "$OUT/sources.txt"
"$JAVA_HOME/bin/javac" --release 11 -nowarn -cp "$CP" -d "$OUT/classes" @"$OUT/sources.txt"

echo "== 4/6 dexing"
find "$OUT/classes" -name '*.class' > "$OUT/classes.txt"
"$JAVA_HOME/bin/java" -Xmx900m -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 \
  --lib "$PLATFORM" --min-api 26 --release --output "$OUT/dex" @"$OUT/classes.txt" \
  "$NPE_JAR" "$NPE_LIBS/jsoup-1.22.2.jar" "$NPE_LIBS/rhino-1.8.1.jar" \
  "$NPE_LIBS/rhino-engine-1.8.1.jar" "$NPE_LIBS/protobuf-javalite-4.35.1.jar"

echo "== 5/6 packaging"
cp "$OUT/base.apk" "$OUT/unaligned.apk"
for d in "$OUT"/dex/classes*.dex; do ( cd "$OUT/dex" && zip -q -X ../unaligned.apk "$(basename "$d")" ); done
"$BT/zipalign" -f -p 4 "$OUT/unaligned.apk" "$OUT/aligned.apk"

echo "== 6/6 signing"
mkdir -p "$(dirname "$KS")"
if [ ! -f "$KS" ]; then
  "$JAVA_HOME/bin/keytool" -genkeypair -keystore "$KS" -alias liquidtube -keyalg RSA \
    -keysize 2048 -validity 10950 -storepass "$KSPASS" -keypass "$KSPASS" \
    -dname "CN=LiquidTube X, OU=Apps, O=LiquidTube X, L=India, C=IN" > /dev/null 2>&1
fi
"$JAVA_HOME/bin/java" -jar "$BT/lib/apksigner.jar" sign --ks "$KS" --ks-key-alias liquidtube \
  --ks-pass "pass:$KSPASS" --key-pass "pass:$KSPASS" \
  --v1-signing-enabled true --v2-signing-enabled true \
  --out "$(dirname "$OUT")/LiquidTubeX.apk" "$OUT/aligned.apk"

echo "== done: $(dirname "$OUT")/LiquidTubeX.apk"
