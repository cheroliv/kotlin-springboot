@file:Suppress("MemberVisibilityCanBePrivate")

import Constants.BLANK

object AppDeps {
    const val implementation = "implementation"
    const val runtimeOnly = "runtimeOnly"
    const val developmentOnly = "developmentOnly"
    const val testImplementation = "testImplementation"
    const val testRuntimeOnly = "testRuntimeOnly"
    const val kapt = "kapt"
    const val annotationProcessor = "annotationProcessor"
    const val testAnnotationProcessor = "testAnnotationProcessor"

    @JvmStatic
    val appModules by lazy {
        setOf(
            implementation to implementationDeps,
            runtimeOnly to runtimeOnlyDeps,
            developmentOnly to developmentOnlyDeps,
            testImplementation to testDeps,
            testRuntimeOnly to testRuntimeOnlyDeps,
            kapt to kaptDeps,
            annotationProcessor to annotationProcessorDeps,
            testAnnotationProcessor to testAnnotationProcessorDeps,
        )
    }

    @JvmStatic
    val implementationDeps by lazy {
        setOf(
            Triple("org.jetbrains.kotlin:kotlin-stdlib-jdk8", BLANK, null),
            Triple("org.jetbrains.kotlin:kotlin-reflect", BLANK, null),
            Triple("org.jetbrains.kotlin:kotlin-stdlib-jdk8", BLANK, null),
            Triple("org.jetbrains.kotlin:kotlin-reflect", BLANK, null),
            Triple("io.projectreactor.kotlin:reactor-kotlin-extensions", BLANK, null),
            Triple("org.jetbrains.kotlinx:kotlinx-coroutines-reactor", BLANK, null),
            Triple("org.jetbrains.kotlinx:kotlinx-serialization-json", "kotlinx_serialization_json.version", null),
            Triple("com.fasterxml.jackson.module:jackson-module-kotlin", BLANK, null),
            Triple("org.apache.commons:commons-lang3", BLANK, null),
            Triple("org.springframework.boot:spring-boot-starter-actuator", BLANK, null),
            Triple("org.springframework.boot:spring-boot-starter-mail", BLANK, null),
            Triple("org.springframework.boot:spring-boot-starter-thymeleaf", BLANK, null),
            Triple("com.mailslurp:mailslurp-client-kotlin", "mailslurp-client-kotlin.version", null),
            Triple("org.springframework.boot:spring-boot-starter-validation", BLANK, null),
            Triple("org.springframework.boot:spring-boot-starter-webflux", BLANK, null),
            Triple("org.springframework.boot:spring-boot-starter-data-r2dbc", BLANK, null),
            Triple("org.springframework.boot:spring-boot-starter-security", BLANK, null),
            Triple("org.springframework.security:spring-security-data", BLANK, null),
            Triple("io.jsonwebtoken:jjwt-impl", "jsonwebtoken.version", null),
            Triple("io.jsonwebtoken:jjwt-jackson", "jsonwebtoken.version", null),
            Triple("io.netty:netty-tcnative-boringssl-static", "boring_ssl.version", null),
            //    implementation("org.springframework.cloud:spring-cloud-gcp-starter-storage")
            //    implementation("io.projectreactor.tools:blockhound" to "blockhound_version",
        ) to setOf(
            "org.junit.vintage" to "junit-vintage-engine",
            "org.springframework.boot" to "spring-boot-starter-tomcat",
            "org.apache.tomcat" to null
        )
    }


    @JvmStatic
    val runtimeOnlyDeps by lazy {
        setOf(
            Triple("org.springframework.boot:spring-boot-properties-migrator", BLANK, null),
            Triple("com.h2database:h2", BLANK, null),
            Triple("io.r2dbc:r2dbc-h2", BLANK, null),
    //        //    runtimeOnly ("com.google.appengine:appengine:+")
    //        //    runtimeOnly("io.r2dbc:r2dbc-postgresql")
    //        //    runtimeOnly("org.postgresql:postgresql")
        ) to null
    }

    @JvmStatic
    val testDeps by lazy {
        setOf(
            Triple("org.jetbrains.kotlin:kotlin-test", BLANK, null),
            Triple("org.jetbrains.kotlin:kotlin-test-junit5", BLANK, null),
    //                Triple(
    //                    "org.springframework.boot:spring-boot-starter-test",
    //                    BLANK,
    //                    setOf(mapOf("mockito-core" to null))
    //                ),
            Triple("io.projectreactor:reactor-test", BLANK, null),
            Triple("org.mockito.kotlin:mockito-kotlin", "mockito_kotlin_version", null),
            Triple("io.mockk:mockk", "mockk.version", null),
            Triple("com.github.tomakehurst:wiremock-jre8", "wiremock.version", null),
            Triple("com.ninja-squad:springmockk", "springmockk.version", null),
    //        //    testImplementation("io.projectreactor.tools:blockhound-junit-platform" to "blockhound_version",
            //    "org.testcontainers:junit-jupiter" to BLANK,
            //    "org.testcontainers:postgresql" to BLANK,
            //    "org.testcontainers:r2dbc" to BLANK,
            //    "com.tngtech.archunit:archunit-junit5-api" to "archunit_junit5_version",
            //     "org.springframework.cloud:spring-cloud-starter-contract-verifier" to BLANK,
        ) to emptySet<Pair<String, String>>()
    }

    @JvmStatic
    val developmentOnlyDeps by lazy {
        setOf(Triple("org.springframework.boot:spring-boot-devtools", BLANK, null)) to null
    }

    @JvmStatic
    val annotationProcessorDeps by lazy {
        setOf(
            Triple("org.springframework.boot:spring-boot-configuration-processor", BLANK, null)
        ) to null
    }

    @JvmStatic
    val kaptDeps by lazy {
        emptySet<Triple<String, String, Set<Map<String, String>>>>() to emptySet<Pair<String, String?>>()
    }

    @JvmStatic
    val testAnnotationProcessorDeps by lazy {
        Pair(emptySet<Triple<String, String, Set<Map<String, String>>>>(), emptySet<Pair<String, String?>>())
    }

    @JvmStatic
    val testRuntimeOnlyDeps by lazy {
        //        //testRuntimeOnly("com.tngtech.archunit:archunit-junit5-engine" to "archunit_junit5_version",
        Pair(emptySet<Triple<String, String, Set<Map<String, String>>>>(), emptySet<Pair<String, String?>>())
    }
}