import java.io.ByteArrayOutputStream


plugins {
    id("java-library")
    id("io.github.goooler.shadow") version "8.1.7"
}

subprojects {
    plugins.apply("java-library")
    plugins.apply("io.github.goooler.shadow")

    group = "${project.property("group")}"
    version = "${project.property("version")}" //.${commitsSinceLastTag()}

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
        withSourcesJar()
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.release = 17
            options.compilerArgs.add("-Xlint:none")
        }

        jar {
            archiveClassifier.set("noshade")
        }

        shadowJar {
            archiveClassifier.set("")
            archiveFileName.set("${project.property("artifactName")}-${project.version}.jar")
        }


        build {
            dependsOn(shadowJar)
        }
    }
}

fun commitsSinceLastTag(): String {
    val tagDescription = ByteArrayOutputStream()
    exec {
        commandLine("git", "describe", "--tags")
        standardOutput = tagDescription
    }
    if (tagDescription.toString().indexOf('-') < 0) {
        return "0"
    }
    return tagDescription.toString().split('-')[1]
}