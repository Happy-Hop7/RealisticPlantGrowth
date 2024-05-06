repositories {
    //mavenCentral()

    // Adventure API
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
}

dependencies {

    implementation(project(":${rootProject.name}-common"))
    implementation(project(":${rootProject.name}-paper"))
    //implementation(project(":${project.name}-folia"))

    implementation(group = "net.kyori", name = "adventure-api", version = "4.16.0")
    implementation(group = "net.kyori", name = "adventure-platform-bukkit", version = "4.3.2")
    implementation(group = "net.kyori", name = "adventure-text-minimessage", version = "4.17.0")

    implementation (group = "dev.dejvokep", name = "boosted-yaml", version = "1.3.5")
    implementation("org.jetbrains:annotations:24.0.0")

}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand(
                    "name" to project.property("artifactName"),
                    "version" to project.version,
                    "group" to project.group,
                    "author" to project.property("author"),
                    "description" to project.property("description"),
                    "website" to project.property("website"),
            )
        }
    }
    shadowJar {
        minimize {}
//            exclude(project(":${rootProject.name}-common"))
//            exclude(project(":${rootProject.name}-paper"))
//        }

        relocate("org.bstats", "${project.group}.${rootProject.name}.lib.bstats")
        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
    }
}


tasks.test {
    useJUnitPlatform()
}