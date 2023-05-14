import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    application
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "fr.sacane"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("net.dv8tion:JDA:5.0.0-beta.9")
    implementation("org.slf4j:slf4j-api:2.0.0")
    implementation("org.slf4j:slf4j-simple:2.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

sourceSets {
    getByName("main") {
        resources {
            srcDirs("src/main/resources")
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
tasks{
    shadowJar {
        archiveBaseName.set("kora")
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())
        destinationDirectory.set(file("out"))
    }
    jar{
        manifest {
            attributes["Main-Class"] = "fr.sacane.bot.kora.ApplicationKt"
        }
        // To avoid the duplicate handling strategy error
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        // To add all of the dependencies
        from(sourceSets.main.get().output)

        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }
    compileJava {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}

application {
    mainClass.set("fr.sacane.bot.kora.ApplicationKt")
}