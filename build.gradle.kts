plugins {
    kotlin("jvm") version "1.6.10"
}

group = "live.myoun.jumper"
version = "1.0-SNAPSHOT"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin="org.jetbrains.kotlin.jvm")

    repositories {
        maven("https://papermc.io/repo/repository/maven-public/")
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.18-R0.1-SNAPSHOT")

        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))
    }
}