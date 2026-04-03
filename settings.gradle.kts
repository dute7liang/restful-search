pluginManagement {
    val intellijPlatformPluginVersion = providers.gradleProperty("intellijPlatformPluginVersion").get()

    repositories {
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/public")
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("org.jetbrains.intellij.platform") version intellijPlatformPluginVersion
    }
}

rootProject.name = "RestfulSearch"
