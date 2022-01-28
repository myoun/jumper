plugins {
    id("org.jetbrains.dokka") version "1.6.10"
}

dependencies {
    implementation("com.github.shynixn.structureblocklib:structureblocklib-bukkit-api:2.3.0")
}

tasks {
    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    register<Jar>("dokkaJar") {
        archiveClassifier.set("javadoc")
        dependsOn("dokkaHtml")

        from("$buildDir/dokka/html/")
    }
}