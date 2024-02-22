import net.neoforged.gradle.dsl.common.runs.run.Run

buildscript {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

plugins {
    id("net.neoforged.gradle.userdev") version "7.0.78"
    id("com.diffplug.spotless") version "6.21.0"
    id("maven-publish")
    java
    idea
}

val modVersion: String by project
val ae2Version: String by project
val ae2wtlibVersion: String by project
val neoforgeVersion: String by project
val curiosVersion: String by project

version = "$modVersion-SNAPSHOT"

val pr = System.getenv("PR_NUMBER") ?: ""
if (pr != "") {
    version = "$modVersion+pr$pr"
}

val tag = System.getenv("TAG") ?: ""
if (tag != "") {
    version = tag
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

dependencies {
    implementation("net.neoforged:neoforge:${neoforgeVersion}")
    implementation("appeng:appliedenergistics2-neoforge:${ae2Version}")
    implementation("maven.modrinth:applied-energistics-2-wireless-terminals:${ae2wtlibVersion}")

    implementation("com.google.code.findbugs:jsr305:3.0.2")
    runtimeOnly("top.theillusivec4.curios:curios-neoforge:${curiosVersion}")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://modmaven.dev/")
        content {
            includeGroup("appeng")
        }
    }
    maven {
        url = uri("https://maven.parchmentmc.net/")
        content {
            includeGroup("org.parchmentmc.data")
        }
    }
    maven {
        url = uri("https://maven.theillusivec4.top/")
        content {
            includeGroup("top.theillusivec4.curios")
        }
    }
    maven {
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
}

tasks {
    processResources {
        val resourceTargets = "META-INF/mods.toml"

        val replaceProperties = mapOf(
            "version" to version as String, "ae2_version" to ae2Version
        )

        inputs.properties(replaceProperties)
        filesMatching(resourceTargets) {
            expand(replaceProperties)
        }
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
}

runs {
    val config = Action<Run> {
        modSource(project.sourceSets.main.get())
    }

    create("client", config)
    create("server", config)
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    java {
        target("/src/*/java/**/*.java")

        endWithNewline()
        indentWithSpaces()
        removeUnusedImports()
        toggleOffOn()
        eclipse().configFile("codeformat/codeformat.xml")
        importOrderFile("codeformat/emergency_storage_card.importorder")
    }
}
