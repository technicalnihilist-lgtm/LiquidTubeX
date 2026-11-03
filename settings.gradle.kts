pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()

        // JitPack builds NewPipeExtractor on demand from its GitHub tag.
        maven(url = "https://jitpack.io") {
            content {
                includeGroup("com.github.teamnewpipe")
            }
        }

        // Maven Central's snapshot repository. Unused by the app now that it
        // pins the JitPack tag, kept for the snapshot route in gradle.properties.
        // Restricted to net.newpipe so snapshots cannot leak into the graph.
        maven(url = "https://central.sonatype.com/repository/maven-snapshots/") {
            content {
                includeGroup("net.newpipe")
            }
        }
    }
}

rootProject.name = "LiquidTubeX"
include(":app")

// ---------------------------------------------------------------------------
// Optional: build NewPipeExtractor from a local checkout instead of the
// published snapshot artifact.
//
// Uncomment this block only when a NewPipeExtractor checkout actually sits next
// to this project (../NewPipeExtractor). Gradle then compiles the extractor from
// source and substitutes it for the module dependency declared in
// app/build.gradle.kts — the version there is ignored.
//
// The `repositories { }` block does NOT belong inside `dependencySubstitution`;
// an included build brings its own repositories and dependency graph.
// ---------------------------------------------------------------------------
// includeBuild("../NewPipeExtractor") {
//     dependencySubstitution {
//         substitute(module("com.github.teamnewpipe:NewPipeExtractor"))
//             .using(project(":extractor"))
//     }
// }
