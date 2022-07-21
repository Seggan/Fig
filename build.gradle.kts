import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "io.github.seggan.fig"
version = "0.3.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.obermuhlner:big-math:2.3.0")
    testImplementation(kotlin("test"))
}

tasks.shadowJar {
    archiveFileName.set("Fig-${project.version}.jar")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("io.github.seggan.fig.MainKt")
}