plugins {
    id("com.diffplug.spotless") version "7.1.0"
    id("com.gradleup.shadow") version "8.3.5"
    id("de.chojo.publishdata") version "1.4.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    java
    `maven-publish`
}

group = "de.eldoria"
version = "1.1.1"

repositories {
    maven("https://eldonexus.de/repository/maven-public/")
    maven("https://eldonexus.de/repository/maven-proxies/")
}

dependencies {
    compileOnly("de.eldoria", "schematicbrushreborn-api", "2.7.3")
    compileOnly("org.spigotmc", "spigot-api", "1.14.4-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit", "worldedit-bukkit", "7.3.15")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.3")
}

spotless {
    java {
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        target("**/*.java")
    }
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

publishData {
    addBuildData()
    useEldoNexusRepos()
    publishComponent("java")
}

publishing {
    publications.create<MavenPublication>("maven") {
        publishData.configurePublication(this)
    }

    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    username = System.getenv("NEXUS_USERNAME")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }

            setUrl(publishData.getRepository())
            name = "EldoNexus"
        }
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    compileTestJava {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    shadowJar {
        val shadebase = "de.eldoria.schematicbrush.libs."
        relocate("de.eldoria.messageblocker", shadebase + "messageblocker")
        relocate("com.jackson", shadebase + "jackson")
        relocate("de.eldoria.eldoutilities", shadebase + "utilities")
        archiveBaseName.set("SchematicTools")
        mergeServiceFiles()
    }

    register<Copy>("copyToServer") {
        val path = project.property("targetDir") ?: "";
        if (path.toString().isEmpty()) {
            println("targetDir is not set in gradle properties")
            return@register
        }
        println("Copying jar to $path")
        from(shadowJar)
        destinationDir = File(path.toString())
    }

    runServer {
        minecraftVersion("1.21.1")
        downloadPlugins {
            url("https://ci.athion.net/job/FastAsyncWorldEdit/lastSuccessfulBuild/artifact/artifacts/FastAsyncWorldEdit-Paper-2.12.4-SNAPSHOT-1013.jar")
            url("https://download.luckperms.net/1569/bukkit/loader/LuckPerms-Bukkit-5.4.152.jar")
        }

        jvmArgs("-Dcom.mojang.eula.agree=true")
    }

    build {
        dependsOn(shadowJar)
    }
}

bukkit {
    name = "SchematicTools"
    main = "de.eldoria.schematictools.SchematicTools"
    apiVersion = "1.16"
    authors = listOf("RainbowDashLabs")
    depend = listOf("SchematicBrushReborn")

    commands {
        register("schematictools") {
            description = "Base command of schematic tools"
            permission = "schematictools.use"
            aliases = listOf("sbt")
        }
    }
}
