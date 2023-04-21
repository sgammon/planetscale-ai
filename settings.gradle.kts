@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        maven("https://gradle.pkg.st/")
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.enterprise") version("3.11.3")
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

val elideVersion: String by settings
val micronautVersion: String by settings

dependencyResolutionManagement {
    repositoriesMode.set(
        RepositoriesMode.PREFER_SETTINGS,
    )
    repositories {
        maven("https://maven.pkg.st/")
        maven("https://elide.pkg.st/")
    }
    versionCatalogs {
        create("mn") {
            from("io.micronaut:micronaut-bom:$micronautVersion")
        }
        create("framework") {
            from("dev.elide:bom:$elideVersion")
        }
    }
}

rootProject.name = "planetscale-ai"

val enableLocalCache = (System.getenv("GRADLE_CACHE_LOCAL")?.toBoolean() ?: true)
val enableRemoteCache = (System.getenv("GRADLE_CACHE_REMOTE")?.toBoolean() ?: true)
val cachePush = (System.getenv("GRADLE_CACHE_PUSH") != "false" || System.getenv("CI") == "true")

buildCache {
    // The local cache is enabled by default, unless you set `GRADLE_CACHE_LOCAL`
    // to `false`. This can be useful for testing the remote cache specifically.
    local {
        isEnabled = enableLocalCache
    }

    // Here we configure Buildless as the Gradle remote cache. The remote cache is
    // enabled by default just like the local cache. Unless `GRADLE_CACHE_PUSH` is
    // set to `false`, artifacts will be pushed up to the cache when they can't be
    // found via GET.
    remote<HttpBuildCache> {
        isEnabled = enableRemoteCache

        // Buildless uses the Expect/Continue pattern to smoothly reject artifacts
        // which are inefficient to cache (because they are too large).
        isUseExpectContinue = true

        // If you prefer not to push unless opted-in, change this conditional
        // to check that `GRADLE_CACHE_PUSH` == `true`. Pushing should always be
        // enabled in CI in order to prime the cache for developer use.
        isPush = cachePush

        // The cache endpoint Gradle uses can be customized on a per-job or
        // per-project basis with the `CACHE_ENDPOINT` variable. This endpoint is
        // Buildless' global anycast API domain.
        url = uri(System.getenv("CACHE_ENDPOINT") ?: "https://global.less.build/cache/generic/")

        credentials {
            username = "apikey"
            password = System.getenv("BUILDLESS_APIKEY")
        }
    }
}
