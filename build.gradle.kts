plugins {
    id("fabric-loom") version "1.4.1"
}

loom {
    splitEnvironmentSourceSets()

    mods {
        create("outasple") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.getByName("client"))
            accessWidenerPath = file("src/main/resources/outasple.accesswidener")
        }
    }
}

repositories {
    maven {
        name = "Parchment"
        url = uri("https://maven.parchmentmc.org/")
        content {
            includeGroupAndSubgroups("org.parchmentmc")
        }
    }
}

dependencies {
    val mcVersion = project.ext["minecraft_version"]
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$mcVersion:2023.06.26@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${project.ext["loader_version"]}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.ext["fabric_version"]}")
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
}

java {
    withSourcesJar()

    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}
