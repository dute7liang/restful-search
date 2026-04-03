plugins {
    java
    id("org.jetbrains.intellij.platform")
}

// 主要构建参数统一从 gradle.properties 读取，便于后续对齐官方模板维护。
val pluginGroupValue = providers.gradleProperty("pluginGroup").get()
val pluginVersionValue = providers.gradleProperty("pluginVersion").get()
val platformVersionValue = providers.gradleProperty("platformVersion").get()
val platformSinceBuildValue = providers.gradleProperty("platformSinceBuild").get()
val javaVersionValue = providers.gradleProperty("javaVersion").get().toInt()
// bundled plugins 在 properties 中使用逗号分隔，这里拆成列表供依赖块复用。
val platformBundledPlugins = providers.gradleProperty("platformBundledPlugins")
    .get()
    .split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersionValue))
    }
}

group = pluginGroupValue
version = pluginVersionValue

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaUltimate(platformVersionValue)
        platformBundledPlugins.forEach { bundledPlugin(it) }
    }

    implementation("com.google.code.gson:gson:${providers.gradleProperty("gsonVersion").get()}")
    implementation("commons-lang:commons-lang:${providers.gradleProperty("commonsLangVersion").get()}")
    implementation("com.fifesoft:rsyntaxtextarea:${providers.gradleProperty("rsyntaxtextareaVersion").get()}")
    implementation("org.yaml:snakeyaml:${providers.gradleProperty("snakeyamlVersion").get()}")
}

intellijPlatform {
    pluginConfiguration {
        version = project.version.toString()

        ideaVersion {
            sinceBuild = platformSinceBuildValue
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask>().configureEach {
    // 2026.1 下 runIde 注入的协程调试 javaagent 会在 JVM 启动前崩溃：
    // ClassNotFoundException: kotlinx.coroutines.debug.internal.AgentPremain
    // 这里显式指向一个不存在的文件，让插件的参数提供器跳过添加 -javaagent，
    // 先保证开发态 IDE 能正常启动；后续如果确实需要协程调试，再单独处理 agent 链路。
    coroutinesJavaAgentFile.set(layout.projectDirectory.file(".intellijPlatform/disabled-coroutines-javaagent.jar"))
}
