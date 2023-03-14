pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm").version(extra["kotlin.version"].toString())
        kotlin("plugin.serialization").version(extra["kotlin.version"].toString())
        kotlin("plugin.allopen").version(extra["kotlin.version"].toString())
        kotlin("plugin.noarg").version(extra["kotlin.version"].toString())
        kotlin("plugin.spring").version(extra["kotlin.version"].toString())
        id("org.springframework.boot").version(extra["springboot.version"].toString())
        id("io.spring.dependency-management").version(extra["spring_dependency_management.version"].toString())
        id("com.google.cloud.tools.jib").version(extra["jib_version"].toString())
        id("com.google.cloud.tools.appengine").version("2.4.4").toString()
        id("com.github.andygoossens.gradle-modernizer-plugin").version(extra["modernizer.version"].toString())
    }
    resolutionStrategy {
        eachPlugin {
            if (requested
                    .id
                    .id
                    .startsWith("com.google.cloud.tools.appengine")
            ) useModule("com.google.cloud.tools:appengine-gradle-plugin:2.4.4")
        }
    }
}

rootProject.name = "kotlin-springboot"
listOf(
    "domain",
    "repositories",
    "spring-data",
    "jooq",
    "core",
    "server",
    "cli",
    "function",
).forEach(::include)