plugins {
    // Java 插件负责常规源码编译能力。
    java
    // IntelliJ Platform Gradle Plugin 负责构建、运行、校验和打包 IDEA 插件。
    id("org.jetbrains.intellij.platform")
}

// 主要构建参数统一从 gradle.properties 读取，便于后续对齐官方模板维护。
// 插件坐标信息，会同步到最终产物的 group/version。
val pluginGroupValue = providers.gradleProperty("pluginGroup").get()
val pluginVersionValue = providers.gradleProperty("pluginVersion").get()
// 目标 IntelliJ 平台版本，例如 2024.3。
val platformVersionValue = providers.gradleProperty("platformVersion").get()
// 插件声明的最低兼容 build 号，例如 243 表示 2024.3。
val platformSinceBuildValue = providers.gradleProperty("platformSinceBuild").get()
// Java 编译目标版本。这里需要和所选 IntelliJ 平台要求保持一致。
val javaVersionValue = providers.gradleProperty("javaVersion").get().toInt()

java {
    toolchain {
        // 强制使用指定版本的 JDK 编译，避免本机默认 JDK 影响构建结果。
        languageVersion.set(JavaLanguageVersion.of(javaVersionValue))
    }
}

group = pluginGroupValue
version = pluginVersionValue

repositories {
    // 常规 Maven 仓库，用于解析 commons-lang、snakeyaml 之类的三方依赖。
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
    intellijPlatform {
        // IntelliJ 平台、Plugin Verifier 和 IDE 安装包都从这里声明的仓库解析。
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // 指定插件编译时依赖的 IDEA 基础平台版本。
        intellijIdeaUltimate(platformVersionValue)
        // 声明依赖的 bundled plugins，等价于 plugin.xml 中的 depends。
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("com.intellij.properties")
        bundledPlugin("org.jetbrains.plugins.yaml")
    }

    // 业务代码直接使用到的第三方库。
    implementation("commons-lang:commons-lang:2.6")
    implementation("org.yaml:snakeyaml:2.4")
}

intellijPlatform {
    pluginConfiguration {
        // 最终插件包显示的版本号。
        version = project.version.toString()

        ideaVersion {
            // 只声明最低兼容版本；不设 untilBuild，表示后续版本先保持开放。
            sinceBuild = platformSinceBuildValue
        }
    }

    pluginVerification {
        ides {
            // recommended() 会选择插件官方推荐的一组 IDE 版本做校验，
            // 不一定等于最低兼容版本；如果需要固定校验 2024.3，需要改成显式指定版本。
            recommended()
        }
    }
}

tasks.withType<JavaCompile> {
    // 统一源码文件编码，避免中文注释在不同机器上出现乱码。
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask>().configureEach {
    // 2026.1 下 runIde 注入的协程调试 javaagent 会在 JVM 启动前崩溃：
    // ClassNotFoundException: kotlinx.coroutines.debug.internal.AgentPremain
    // 这里显式指向一个不存在的文件，让插件的参数提供器跳过添加 -javaagent，
    // 先保证开发态 IDE 能正常启动；后续如果确实需要协程调试，再单独处理 agent 链路。
    coroutinesJavaAgentFile.set(layout.projectDirectory.file(".intellijPlatform/disabled-coroutines-javaagent.jar"))
}
