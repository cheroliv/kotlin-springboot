@file:Suppress(
    "GradlePackageUpdate",
    "DEPRECATION", "LocalVariableName",
)

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
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    }
}

plugins {
    jacoco
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.allopen")
    kotlin("plugin.noarg")
    kotlin("plugin.serialization")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.google.cloud.tools.jib")
    id("com.google.cloud.tools.appengine")
    id("com.github.andygoossens.gradle-modernizer-plugin")
}

group = properties["artifact.group"].toString()
version = properties["artifact.version"].toString()

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${properties["kotlinx_serialization_json.version"]}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("com.mailslurp:mailslurp-client-kotlin:${properties["mailslurp-client-kotlin.version"]}")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-data")
    implementation("io.jsonwebtoken:jjwt-impl:${properties["jsonwebtoken.version"]}")
    implementation("io.jsonwebtoken:jjwt-jackson:${properties["jsonwebtoken.version"]}")
    implementation("io.netty:netty-tcnative-boringssl-static:${properties["boring_ssl.version"]}")

    //    implementation("org.springframework.cloud:spring-cloud-gcp-starter-storage")
    //    implementation("io.projectreactor.tools:blockhound:${properties["blockhound_version"]}")


    runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("io.r2dbc:r2dbc-h2")
    //        //    runtimeOnly ("com.google.appengine:appengine:+")
    //        //    runtimeOnly("io.r2dbc:r2dbc-postgresql")
    //        //    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${properties["mockito_kotlin_version"]}")
    testImplementation("io.mockk:mockk:${properties["mockk.version"]}")
    testImplementation("com.github.tomakehurst:wiremock-jre8:${properties["wiremock.version"]}")
    testImplementation("com.ninja-squad:springmockk:${properties["springmockk.version"]}")
    //        //    testImplementation("io.projectreactor.tools:blockhound-junit-platform" to "blockhound_version"]}")
    //    "org.testcontainers:junit-jupiter")
    //    "org.testcontainers:postgresql")
    //    "org.testcontainers:r2dbc")
    //    "com.tngtech.archunit:archunit-junit5-api" to "archunit_junit5_version")
    //     "org.springframework.cloud:spring-cloud-starter-contract-verifier")
//    testRuntimeOnly("com.tngtech.archunit:archunit-junit5-engine:${properties["archunit_junit5_version"]}")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
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

//java.sourceCompatibility = JavaVersion.VERSION_8

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(properties["free_compiler_args_value"].toString())
        jvmTarget = VERSION_19.toString()
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
        html.required.set(true)
        ignoreFailures = true
    }
}

val Project.sep: String  get() = getProperty("file.separator")

tasks.register<Delete>("cleanResources") {
    description = "Delete directory build/resources"
    group = "build"
    delete(buildString {
        append("build")
        append(sep)
        append("resources")
    })
}

tasks.register<TestReport>("testReport") {
    description = "Generates an HTML test report from the results of testReport task."
    group = "report"
    destinationDirectory.set(file(buildString {
        append(buildDir)
        append(sep)
        append("reports")
        append(sep)
        append("tests")
    }))
    reportOn("test")
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