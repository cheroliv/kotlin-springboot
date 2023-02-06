@file:Suppress(
    "GradlePackageUpdate",
    "DEPRECATION",
)


import org.gradle.api.JavaVersion.VERSION_17
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import GradleUtils.appDependencies
import Versions.kotlin_version

buildscript {
    repositories {
        gradlePluginPortal()
        google()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin_version}")
    }
}

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
    kotlin("plugin.serialization")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.google.cloud.tools.jib")
    id("com.github.andygoossens.gradle-modernizer-plugin")
    id("com.google.cloud.tools.appengine")
    jacoco
}

group = properties["artifact.group"].toString()
version = properties["artifact.version"].toString()

//TODO: CLI apiclient to setup mailsurp
//create 2 inboxes: signup,password
tasks.register<DefaultTask>("addMailSlurpConfiguration") {
    group = "application"
    description = "add a yaml spring configuration for mailSlurp properties, and add the file to .gitignore"
    doFirst { println("addMailSlurpConfiguration") }
    //TODO: addMailSlurpConfiguration task
//check if src/main/resources/application-mailslurp.yml exists?
//when src/main/resources/application-mailslurp.yml dont exists then create file
//check if .gitignore exists?
//when .gitignore dont exists then create file
// and add src/main/resources/application-mailslurp.yml into .gitignore
//when .gitgnore exists then check if src/main/resources/application-mailslurp.yml is found into .gitignore
//when src/main/resources/application-mailslurp.yml is not found into .gitignore
// then add src/main/resources/application-mailslurp.yml to .gitgnore
}

//springBoot.mainClass.set("webapp.BackendBootstrap")
///*
//./gradlew -q cli --args='your args there'
// */
//tasks.register("cli") {
//    group = "application"
//    description = "Run webapp cli"
//    doFirst { springBoot.mainClass.set("webapp.CliBootstrap") }
//    finalizedBy("bootRun")
//}
repositories {
    google()
    mavenCentral()
}

dependencies {
    appDependencies()
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
        jvmTarget = VERSION_17.toString()
    }
}

modernizer {
    failOnViolations = true
    includeTestClasses = true
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging { events(FAILED, SKIPPED) }
    reports {
        html.isEnabled = true
        ignoreFailures = true
    }
}

tasks.register<Delete>("cleanResources") {
    description = "Delete directory build/resources"
    group = "build"
    delete("build/resources")
}

tasks.register<TestReport>("testReport") {
    description = "Generates an HTML test report from the results of testReport task."
    group = "report"
    destinationDir = file("$buildDir/reports/tests")
    reportOn("test")
}

jib {
    from {
        image = "eclipse-temurin@sha256:fabe27bd9db502d484a11d3f571c2f4ef7bba4a172527084d939935358fb06c4"
        platforms {
            platform {
                architecture = "${findProperty("jibArchitecture") ?: "amd64"}"
                os = "linux"
            }
        }
        auth {
            username = properties["docker_hub_login"].toString()
            password = properties["docker_hub_password"].toString()
        }
    }

    to {
        image = "cheroliv/kotlin-springboot"
//        auth {
//            username = properties["docker_hub_login_token"].toString()
//            password = properties["docker_hub_password"].toString()
//        }
//        auth {
//            username = properties["docker_hub_email"].toString()
//            password = properties["docker_hub_password"].toString()
//        }
    }
}