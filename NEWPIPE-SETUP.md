# NewPipeExtractor — Gradle setup for LiquidTube

LiquidTube is built today by `build.sh` (aapt2 + javac + d8), so it has no Gradle
build. The files in this archive add one **alongside** `build.sh` — the shell
script keeps working exactly as before.

## Where each file goes

```
LiquidTube/
├── settings.gradle.kts        <- new (root)
├── build.gradle.kts           <- new (root)
├── gradle.properties          <- new (root)
├── gradle/wrapper/gradle-wrapper.properties   <- new
├── app/
│   ├── build.gradle.kts       <- new (app module)
│   ├── AndroidManifest.xml    <- unchanged
│   ├── java/  res/  assets/   <- unchanged
└── build.sh                   <- unchanged
```

## One edit before the first build

In `gradle.properties`, set the extractor version to the short commit hash of the
NewPipeExtractor commit you want:

```
extractorVersion=66d9ee1-SNAPSHOT
```

(`git rev-parse --short HEAD` inside the extractor repo. Snapshot artifacts stay
on Maven Central for 90 days.)

## Building

The wrapper JAR is not included (it is generated, not checked in):

```
gradle wrapper --gradle-version 7.6.4
./gradlew :app:assembleRelease
```

Then sign `app/build/outputs/apk/release/app-release-unsigned.apk` with the same
keystore `build.sh` uses, or add a `signingConfigs` block. AGP 7.4.2 is pinned
because it still reads the package name from `AndroidManifest.xml`; AGP 8 removes
that and would break `build.sh`.

## The two ways to depend on the extractor — pick one

**1. JitPack release (default, nothing to uncomment).**
`settings.gradle.kts` declares `https://jitpack.io`, limited to the
`com.github.teamnewpipe` group, and `app/build.gradle.kts` declares
`implementation("com.github.teamnewpipe:NewPipeExtractor:v0.26.5")`.

The alternative is a Maven Central snapshot: `settings.gradle.kts` also declares
`https://central.sonatype.com/repository/maven-snapshots/` for the `net.newpipe`
group, and `gradle.properties` carries the `extractorVersion` hash for it.

**2. Local source checkout (composite build).**
Uncomment the `includeBuild("../NewPipeExtractor")` block in
`settings.gradle.kts`. Gradle compiles the extractor from `../NewPipeExtractor`
and substitutes it for the module above; the version string is then ignored, so
you can leave `extractorVersion` as it is.

Notes on the block as it is written:

- `repositories { }` is not valid inside `dependencySubstitution { }`. An
  included build brings its own repositories and its own dependency graph.
- `substitute(module(...))` must name the coordinate the app actually declares,
  i.e. `com.github.teamnewpipe:NewPipeExtractor`, or the substitution silently
  never matches.
- `includeBuild` only accepts a path that exists. If `../NewPipeExtractor` is
  missing, configuration fails, which is why the block ships commented out.

## What is still missing

These files only put the library on the classpath. Nothing in the app calls it
yet: `MainActivity` is a pure WebView shell, so actually using the extractor
needs a JavaScript bridge (`addJavascriptInterface`) that runs the extractor off
the main thread and hands stream URLs to the HTML UI.
