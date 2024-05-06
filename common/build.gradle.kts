repositories{
    // Spigot API
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    mavenLocal()
}

dependencies {
    compileOnly(group = "com.google.code.gson", name = "gson", version = "2.8.9")
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")

    testImplementation(group = "com.google.code.gson", name = "gson", version = "2.10.1")

    implementation(group = "org.bstats", name = "bstats-bukkit", version = "3.0.2")
    implementation("org.jetbrains:annotations:24.0.0")

    testImplementation(group = "junit", name = "junit", version = "4.13.2")
}
