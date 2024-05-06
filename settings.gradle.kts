rootProject.name = "RealisticPlantGrowth"

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

sequenceOf(
        "common",
        "paper",
        "spigot"
).forEach {
    include("${rootProject.name}-$it")
    project(":${rootProject.name}-$it").projectDir = file(it)
    println(include("${rootProject.name}-$it"))
}
// include("RealisticPlantGrowth-common")
// include("RealisticPlantGrowth-paper")
//include("spigot")
