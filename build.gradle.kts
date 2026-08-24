plugins {
    id("com.diffplug.spotless") version "8.1.0"
    id("com.gradleup.shadow") version "9.5.1"
    id("de.chojo.publishdata") version "1.4.0"
    id("de.eldoria.plugin-yml.bukkit") version "0.9.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    java
    `maven-publish`
}

group = "de.eldoria"
version = "1.1.2"

repositories {
    maven("https://eldonexus.de/repository/maven-public/")
    maven("https://eldonexus.de/repository/maven-proxies/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("de.eldoria", "schematicbrushreborn-api", "2.7.10")
    compileOnly("io.papermc.paper", "paper-api", "26.2.build.116-stable")
    // Exclude Guava and Gson from WorldEdit so their {strictly} version constraints
    // (pinned to the versions shipped by older Minecraft) don't clash with the newer
    // versions required by Paper 26.2. Both libraries are provided by the Paper
    // server at runtime, so excluding them here only affects compile-time resolution.
    compileOnly("com.sk89q.worldedit", "worldedit-bukkit", "7.3.18") {
        exclude(group = "com.google.guava")
        exclude(group = "com.google.code.gson")
    }

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.4")
}

// Belt-and-suspenders: force the Guava/Gson versions shipped by Paper 26.2 across
// every configuration, overriding any remaining transitive {strictly} constraints.
configurations.all {
    resolutionStrategy.force(
        "com.google.guava:guava:33.6.0-jre",
        "com.google.code.gson:gson:2.14.0"
    )
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
        languageVersion = JavaLanguageVersion.of(25)
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
        relocate("com.fasterxml", shadebase + "fasterxml")
        relocate("de.eldoria.jacksonbukkit", shadebase + "jacksonbukkit")
        relocate("de.eldoria.eldoutilities", shadebase + "utilities")
        // Fork build: use a distinct jar name so it cannot be mistaken for the
        // upstream SchematicTools release.
        archiveBaseName.set("SchematicToolsFork")
        mergeServiceFiles()
    }

    runServer {
        minecraftVersion("26.2")
        downloadPlugins {
            url("https://ci.athion.net/job/FastAsyncWorldEdit/1231/artifact/artifacts/FastAsyncWorldEdit-Paper-2.14.3-SNAPSHOT-1231.jar")
            url("https://download.luckperms.net/1611/bukkit/loader/LuckPerms-Bukkit-5.5.22.jar")
        }

        jvmArgs("-Dcom.mojang.eula.agree=true")
    }

    build {
        dependsOn(shadowJar)
    }
}

bukkit {
    // Fork build: rename the plugin so it is clearly distinct from the upstream
    // SchematicTools. The plugin data folder becomes plugins/SchematicToolsFork/.
    name = "SchematicToolsFork"
    main = "de.eldoria.schematictools.SchematicTools"
    apiVersion = "26.2"
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
