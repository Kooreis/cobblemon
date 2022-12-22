plugins {
    id("cobblemon.platform-conventions")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val generatedResources = file("src/generated/resources")

sourceSets {
    main {
        resources {
            srcDir(generatedResources)
        }
    }
}

repositories {
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    "developmentFabric"(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    bundle(project(path = ":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }

    modApi(libs.fabricApi)
    modApi(libs.fabricKotlin)
    modApi(libs.architecturyFabric)
    modApi(libs.fabricPermissionsApi)

    listOf(
        libs.stdlib,
        libs.reflect,
        libs.jetbrainsAnnotations,
        libs.serializationCore,
        libs.serializationJson,
        libs.graal,
        libs.molang,
        libs.mclib
    ).forEach {
        bundle(it)
        runtimeOnly(it)
    }

}

tasks {
    // The AW file is needed in :fabric project resources when the game is run.
    val copyAccessWidener by registering(Copy::class) {
        from(loom.accessWidenerPath)
        into(generatedResources)
    }

    shadowJar {}

    processResources {
        dependsOn(copyAccessWidener)
        inputs.property("version", rootProject.version)

        filesMatching("fabric.mod.json") {
            expand("version" to rootProject.version)
        }
    }
}