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

        // Maven Central's snapshot repository. Restricted to the net.newpipe group
        // so snapshots can never leak into the rest of the dependency graph.
        maven(url = "https://central.sonatype.com/repository/maven-snapshots/") {
            content {
                includeGroup("net.newpipe")
            }
        }
    }
}

rootProject.name = "LiquidTube"
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
//         substitute(module("net.newpipe:extractor"))
//             .using(project(":extractor"))
//     }
// }
