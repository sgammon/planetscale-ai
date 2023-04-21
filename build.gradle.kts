import com.github.gradle.node.npm.task.NpmTask
import io.micronaut.gradle.MicronautRuntime
import io.micronaut.gradle.MicronautTestRuntime
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    id("org.jetbrains.kotlin.kapt") version "1.8.20"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.8.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.micronaut.application") version "3.7.8"
    id("io.micronaut.test-resources") version "3.7.8"
    id("com.google.cloud.tools.jib") version "2.8.0"
    id("com.github.node-gradle.node") version "3.5.1"
    id("com.github.ben-manes.versions") version "0.46.0"
}

version = "0.1"
group = "io.github.sgammon"

val workers = listOf(
    "openapi",
    "wellknown",
)

val kotlinVersion: String by properties
val kotlinLanguageVersion: String by properties
val javaLanguageVersion: String by properties
val elideVersion: String by properties
val graalvmVersion: String by properties
val jvmImageCoordinates: String by properties
val nativeImageCoordinates: String by properties

dependencies {
    kapt("io.micronaut.data:micronaut-data-processor")
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.openapi:micronaut-openapi")
    kapt("io.micronaut.serde:micronaut-serde-processor")
    implementation("dev.elide:elide-base:$elideVersion")
    implementation("dev.elide:elide-core:$elideVersion")
    implementation("dev.elide:elide-server:$elideVersion")
    implementation("org.graalvm.sdk:graal-sdk:$graalvmVersion")
    implementation("com.theokanning.openai-gpt3-java:api:0.12.0")
    implementation("com.theokanning.openai-gpt3-java:service:0.12.0")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut.data:micronaut-data-jdbc")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("io.micronaut:micronaut-validation")
    implementation("mysql:mysql-connector-java")
    runtimeOnly("ch.qos.logback:logback-classic")
    compileOnly("org.graalvm.nativeimage:svm")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
}

/**
 * Runtime/Tooling Configuration
 */

application {
    mainClass.set("io.github.sgammon.ApplicationKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

node {
    download.set(false)
    version.set("18.11.0")
}

micronaut {
    runtime(MicronautRuntime.NETTY)
    testRuntime(MicronautTestRuntime.KOTEST_4)
    processing {
        incremental(true)
        annotations("io.github.sgammon.*")
    }
}

graalvmNative {
    agent {
        defaultMode.set("standard")
        enabled.set(true)
    }
}


/**
 * Build: Server
 */

fun KotlinJvmOptions.kotlincConfig() {
    languageVersion = kotlinLanguageVersion
    jvmTarget = javaLanguageVersion
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

    dockerBuild {
        images.add(jvmImageCoordinates)
    }

    dockerBuildNative {
        images.add(nativeImageCoordinates)
    }
}

graalvmNative.toolchainDetection.set(false)

jib {
    from {
        image = "us-docker.pkg.dev/elide-fw/tools/jdk19:latest"
    }
    to {
        image = "us-docker.pkg.dev/planetscale-ai/plugin/jvm:latest"
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


tasks.build {
    dependsOn(buildWorkersTask)
}


/**
 * Publish/Deploy Tasks
 */

val jibtask = tasks.named("jib")

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
