plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    id("org.jetbrains.kotlin.kapt") version "1.8.20"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.8.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.micronaut.application") version "3.7.8"
    id("io.micronaut.test-resources") version "3.7.8"
    id("com.google.cloud.tools.jib") version "2.8.0"
}

version = "0.1"
group = "io.github.sgammon"

val kotlinVersion=project.properties.get("kotlinVersion")
repositories {
    mavenCentral()
}

dependencies {
    kapt("io.micronaut.data:micronaut-data-processor")
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.openapi:micronaut-openapi")
    kapt("io.micronaut.serde:micronaut-serde-processor")
    implementation("com.theokanning.openai-gpt3-java:api:0.12.0")
    implementation("com.theokanning.openai-gpt3-java:client:0.12.0")
    implementation("com.theokanning.openai-gpt3-java:service:0.12.0")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut.data:micronaut-data-jdbc")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("mysql:mysql-connector-java")
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
