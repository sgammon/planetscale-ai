@file:Suppress(
    "DSL_SCOPE_VIOLATION",
    "UnstableApiUsage",
)

import com.github.gradle.node.npm.task.NpmTask
import io.micronaut.gradle.MicronautRuntime
import io.micronaut.gradle.MicronautTestRuntime
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    alias(libs.plugins.jib)
    alias(libs.plugins.node)
    alias(libs.plugins.versions)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.sonar)
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.micronaut.aot)
    alias(libs.plugins.micronaut.testResources)

    // use kotlin helper
    kotlin("jvm") version libs.versions.kotlin.get()
    kotlin("kapt") version libs.versions.kotlin.get()
    kotlin("plugin.allopen") version libs.versions.kotlin.get()
}

version = "0.1"
group = "io.github.sgammon"

val workers = listOf(
    "openapi",
    "wellknown",
)

val kotlinVersion: String by properties
val kotlinLanguageVersion: String by properties
val micronautVersion: String by properties
val nodeVersion: String by properties
val javaLanguageVersion: String by properties
val elideVersion: String by properties
val graalvmVersion: String by properties
val jvmImageCoordinates: String by properties
val nativeImageCoordinates: String by properties
val dockerBaseJvm: String by properties
val dockerBaseNative: String by properties

dependencies {
    // Annotation Processors
    kapt(mn.micronaut.data.processor)
    kapt(mn.micronaut.http.validation)
    kapt(mn.micronaut.openapi)
    kapt(mn.micronaut.serde.processor)

    // Elide
    implementation(framework.elide.server)

    // Open AI API
    implementation(libs.bundles.openai)

    // Micronaut
    implementation(mn.micronaut.jackson.databind)
    implementation(mn.micronaut.data.jdbc)
    implementation(mn.micronaut.kotlin.runtime)
    implementation(mn.micronaut.kotlin.extension.functions)
    implementation(mn.micronaut.serde.jackson)
    implementation(mn.micronaut.jdbc.hikari)
    implementation(mn.swagger.annotations)
    implementation(mn.micronaut.validation)
    implementation(mn.mysql.connector.java)

    // Kotlin
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    // Logback
    runtimeOnly(mn.logback.core)
    runtimeOnly(mn.jackson.module.kotlin)

    // GraalVM
    compileOnly(libs.bundles.graalvm)
}

/**
 * Runtime/Tooling Configuration
 */

application {
    mainClass.set("io.github.sgammon.ApplicationKt")
}

node {
    download.set(false)
    version.set(nodeVersion)
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

kotlin {
    jvmToolchain(19)
}

kover {
    disabledForProject = false
    useKoverTool()
}

micronaut {
    version(
        micronautVersion,
    )
    runtime(
        MicronautRuntime.NETTY,
    )
    testRuntime(
        MicronautTestRuntime.JUNIT_5,
    )
    processing {
        incremental(true)
        annotations("io.github.sgammon.*")
    }
    aot {
        version.set(libs.versions.micronaut.aot.get())
        configFile.set(file("$rootDir/gradle/aot-native.properties"))
        targetEnvironments.addAll("cloud", "live")
        replaceLogbackXml.set(false)
        optimizeServiceLoading.set(true)
        convertYamlToJava.set(true)
        precomputeOperations.set(true)
        cacheEnvironment.set(false) // env is overidden in prod with secret values
        optimizeClassLoading.set(true)
        deduceEnvironment.set(true)
        optimizeNetty.set(true)

        netty {
            enabled.set(true)
        }
    }
}

graalvmNative {
    agent {
        defaultMode.set("standard")
        enabled.set(true)
    }
}

/**
 * Static Analysis Configuration
 */

ktlint {
    version.set(libs.versions.ktlint.get())
}

detekt {
    toolVersion = libs.versions.detekt.get()
    parallel = true
    config.from("$rootDir/.github/detekt.yml")
}

sonar {
    properties {
        listOf(
            "sonar.coverage.jacoco.xmlReportPaths" to listOf("$buildDir/reports/kover/xml/report.xml"),
            "sonar.projectKey" to "sgammon_planetscale-ai",
            "sonar.organization" to "sam-g",
            "sonar.host.url" to "https://sonarcloud.io",
        ).forEach {
            property(it.first, it.second)
        }
    }
}

/**
 * Build: Server
 */

fun KotlinJvmOptions.kotlincConfig() {
    languageVersion = kotlinLanguageVersion
    jvmTarget = javaLanguageVersion
    allWarningsAsErrors = true
}

tasks {
    compileKotlin {
        kotlinOptions {
            kotlincConfig()
        }
    }

    compileTestKotlin {
        kotlinOptions {
            kotlincConfig()
        }
    }

    dockerfile {
        from(dockerBaseJvm)
    }

    optimizedDockerfile {
        from(dockerBaseJvm)
    }

    dockerfileNative {
        from(dockerBaseNative)
    }

    optimizedDockerfileNative {
        from(dockerBaseNative)
    }

    dockerBuild {
        images.add(jvmImageCoordinates)
    }

    optimizedDockerBuild {
        images.add(jvmImageCoordinates)
    }

    dockerBuildNative {
        images.add(nativeImageCoordinates)
    }

    optimizedDockerBuildNative {
        images.add(nativeImageCoordinates)
    }
}

graalvmNative.toolchainDetection.set(false)

jib {
    from {
        image = dockerBaseJvm
    }
    to {
        image = jvmImageCoordinates
    }
}

/**
 * Build: Workers
 */

fun NpmTask.projectInputsOutputs(vararg additionalDependencies: Any) {
    dependsOn(tasks.npmInstall, *additionalDependencies)
    inputs.dir("node_modules")
    inputs.files("tsconfig.json", "package.json")
}

fun NpmTask.workerOutputs() {
    projectInputsOutputs(buildWorkersTask)
    workers.forEach { workerName ->
        inputs.dir("workers/$workerName/build/worker")
    }
}

val prettierCheck = tasks.register<NpmTask>("checkPrettier") {
    group = "check"
    description = "Check JS/TS code style with Prettier"
    args.set(listOf("run", "lint"))
    projectInputsOutputs()
}

val prettierFormat = tasks.register<NpmTask>("formatPrettier") {
    group = "format"
    description = "Format JS/TS code style with Prettier"
    args.set(listOf("run", "format"))
    projectInputsOutputs()
}

val buildWorkersTask = tasks.register<NpmTask>("buildJs") {
    group = "build"
    description = "Build JS targets via Node/NPM"
    args.set(listOf("run", "build"))
    projectInputsOutputs()
    workers.forEach { workerName ->
        inputs.dir(project.fileTree("workers/$workerName").exclude("**/*.spec.ts"))
        outputs.dir("workers/$workerName/build/worker")
    }
}

val publishWorkerStagingTask = tasks.register<NpmTask>("publishWorkersStaging") {
    group = "publish"
    description = "Publish CloudFlare Workers to staging environments"
    args.set(listOf("run", "publish:staging"))
    workerOutputs()
}

val publishWorkerLiveTask = tasks.register<NpmTask>("publishWorkersLive") {
    group = "publish"
    description = "Publish CloudFlare Workers to live environments"
    args.set(listOf("run", "publish:live"))
    workerOutputs()
}

/**
 * Top-level Tasks
 */

val detectCheck: TaskProvider<Task> = tasks.named("detekt")
val ktlintCheck: TaskProvider<Task> = tasks.named("ktlintCheck")
val ktlintFormat: TaskProvider<Task> = tasks.named("ktlintFormat")

tasks.build {
    dependsOn(
        buildWorkersTask,
    )
}

tasks.check {
    dependsOn(
        detectCheck,
        prettierCheck,
        ktlintCheck,
    )
}

tasks.create("format") {
    dependsOn(
        prettierFormat,
        ktlintFormat,
    )
}

listOf("buildLayers").forEach {
    tasks.named(it) {
        doNotTrackState("too big for build cache")
    }
}

/**
 * Publish/Deploy Tasks
 */

val jibtask: TaskProvider<Task> = tasks.named("jib")

tasks.create("publishStaging") {
    group = "publish"
    description = "Publish or deploy all live targets"
    dependsOn(
        publishWorkerStagingTask,
    )
}

tasks.create("publish") {
    group = "publish"
    description = "Publish or deploy all live targets"
    dependsOn(
        publishWorkerLiveTask,
        jibtask,
    )
}
