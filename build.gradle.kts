import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    id("org.jetbrains.kotlin.kapt") version "1.8.20"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.8.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.micronaut.application") version "3.7.8"
    id("io.micronaut.test-resources") version "3.7.8"
    id("com.google.cloud.tools.jib") version "2.8.0"
    id("com.github.node-gradle.node") version "3.5.1"
}

version = "0.1"
group = "io.github.sgammon"

val kotlinVersion: String by properties
val elideVersion: String by properties
val graalvmVersion: String by properties

repositories {
    maven("https://elide-snapshots.storage-download.googleapis.com/repository/v3/")
    mavenCentral()
}

node {
    download.set(true)
    version.set("18.11.0")
}

dependencies {
    kapt("io.micronaut.data:micronaut-data-processor")
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.openapi:micronaut-openapi")
    kapt("io.micronaut.serde:micronaut-serde-processor")
    implementation("dev.elide:base:$elideVersion")
    implementation("dev.elide:core:$elideVersion")
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
    implementation("mysql:mysql-connector-java")
    runtimeOnly("ch.qos.logback:logback-classic")
    compileOnly("org.graalvm.nativeimage:svm")

    implementation("io.micronaut:micronaut-validation")

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

}


application {
    mainClass.set("io.github.sgammon.ApplicationKt")
}
java {
    sourceCompatibility = JavaVersion.toVersion("17")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
    dockerBuild {
        images.add("us-docker.pkg.dev/planetscale-ai/plugin/jvm:latest")
    }

    dockerBuildNative {
        images.add("us-docker.pkg.dev/planetscale-ai/plugin/native:latest")
    }
}
graalvmNative.toolchainDetection.set(false)
micronaut {
    runtime("netty")
    testRuntime("kotest")
    processing {
        incremental(true)
        annotations("io.github.sgammon.*")
    }
}

val buildWorkersTask = tasks.register<NpmTask>("build:workers") {
    args.set(listOf("run", "--prod"))
    dependsOn(tasks.npmInstall)
    inputs.dir(project.fileTree("workers").exclude("**/*.spec.ts"))
    inputs.dir("node_modules")
    inputs.files("tsconfig.json")
    outputs.dir("${project.buildDir}/workers")
}

tasks {
  jib {
    from {
      image = "us-docker.pkg.dev/elide-fw/tools/jdk19:latest"
    }
    to {
      image = "us-docker.pkg.dev/planetscale-ai/plugin/jvm:latest"
    }
  }
}

graalvmNative {
    agent {
        defaultMode.set("standard")
        enabled.set(true)
    }
}
