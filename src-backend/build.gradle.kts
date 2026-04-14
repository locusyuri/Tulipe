plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.10.1"
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("src-backend")
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
description = "src-backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(19)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
