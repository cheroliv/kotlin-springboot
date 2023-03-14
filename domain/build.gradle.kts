import org.gradle.api.JavaVersion.VERSION_19
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.lang.System.getProperty

buildscript {
    repositories {
        gradlePluginPortal()
        google()
    }
    dependencies { classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10") }
}

plugins {
    jacoco
    kotlin("jvm")
    kotlin("plugin.serialization")
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
    id("com.github.andygoossens.gradle-modernizer-plugin")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    setOf(
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
        "org.jetbrains.kotlin:kotlin-reflect",
        "org.jetbrains.kotlinx:kotlinx-serialization-json:${properties["kotlinx_serialization_json.version"]}",
        "org.apache.commons:commons-lang3:${properties["common-lang3.version"]}",
        "com.fasterxml.jackson.module:jackson-module-kotlin:${properties["kotlin-jackson.version"]}",
        "jakarta.validation:jakarta.validation-api:${properties["jakarta.validation-api.version"]}"
    ).forEach(::implementation)
}

modernizer {
    failOnViolations = true
    includeTestClasses = true
}


configurations {
    compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
    implementation.configure {
        setOf(
            "org.junit.vintage" to "junit-vintage-engine",
            "org.springframework.boot" to "spring-boot-starter-tomcat",
            "org.apache.tomcat" to null
        ).forEach { exclude(it.first, it.second) }
    }
}


tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(properties["free_compiler_args_value"].toString())
        jvmTarget = VERSION_19.toString()
    }
}



tasks.withType<Test> {
    useJUnitPlatform()
    testLogging { events(FAILED, SKIPPED) }
    reports {
        html.required.set(true)
        ignoreFailures = true
    }
}

tasks.register<Delete>("cleanResources") {
    description = "Delete directory build/resources"
    group = "build"
    val sep: String = getProperty("file.separator")
    delete(buildString {
        append("build")
        append(sep)
        append("resources")
    })
}

tasks.register<TestReport>("testReport") {
    description = "Generates an HTML test report from the results of testReport task."
    group = "report"
    val sep: String = getProperty("file.separator")
    destinationDirectory.set(file(buildString {
        append(buildDir)
        append(sep)
        append("reports")
        append(sep)
        append("tests")
    }))
    testResults.from("test")
}
