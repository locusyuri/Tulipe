plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.10.1"
}

// Native 相关任务执行时不引入 SpringDoc，避免 GraalVM 打包阶段增加额外处理成本。
val isNativeBuild = gradle.startParameter.taskNames.any { taskName ->
    taskName.contains("native", ignoreCase = true)
}

/**
 * 配置 GraalVM Native
 */
graalvmNative {
    binaries {
        named("main") {
            imageName.set("TulipeBackend")
            mainClass.set("org.fleur.srcbackend.SrcBackendApplicationKt")
            fallback.set(false)
            buildArgs.add("-H:+ReportExceptionStackTraces")
            buildArgs.add("-H:-CheckToolchain")
            buildArgs.add("--enable-preview")
        }
    }
}

group = "org.fleur"
version = "0.0.1-SNAPSHOT"
description = "TulipeBackend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

/**
 * 依赖项
 */
dependencies {
    implementation("org.springframework.boot:spring-boot-starter") // Spring Boot Starter, 用于启动
    implementation("org.springframework.boot:spring-boot-starter-web") // Spring Boot Starter Web, 用于 Web 开发
    implementation("org.springframework.boot:spring-boot-starter-jdbc") // Spring JDBC, 用于 JdbcTemplate
    implementation("org.jetbrains.kotlin:kotlin-reflect") // Kotlin Reflect, 用于反射
    if (!isNativeBuild) {
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0") // JVM 调试时使用 Swagger/OpenAPI
        developmentOnly("org.springframework.boot:spring-boot-devtools") // JVM 开发期热重载，native 打包自动忽略
    }
    testImplementation("org.springframework.boot:spring-boot-starter-test") // Spring Boot Starter Test, 用于测试
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5") // Kotlin Test JUnit5, 用于测试
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") // JUnit Platform Launcher, 用于运行测试
    runtimeOnly("org.xerial:sqlite-jdbc:3.46.1.3") // SQLite JDBC Driver
    runtimeOnly("com.mysql:mysql-connector-j") // MySQL JDBC Driver
    runtimeOnly("org.postgresql:postgresql") // PostgreSQL JDBC Driver
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
