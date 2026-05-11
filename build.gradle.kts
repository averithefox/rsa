import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("net.fabricmc.fabric-loom-remap")
  kotlin("jvm") version "2.3.21"
  kotlin("plugin.serialization") version "2.3.21"
  id("com.gradleup.shadow") version "9.4.1"
  id("com.github.jmongard.git-semver-plugin") version "0.18.0"
}

semver {
  groupVersionIncrements = false
}

version = semver.infoVersion
val minecraftVersion: String by project
val loaderVersion: String by project
val fabricApiVersion: String by project
val fabricKotlinVersion: String by project
val lwjglVersion: String by project
val commodoreVersion: String by project

repositories {
  maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
  maven("https://jitpack.io")
}

val shadowImplementation by configurations.creating {
  configurations.implementation {
    extendsFrom(this@creating)
  }
}

dependencies {
  minecraft("com.mojang:minecraft:$minecraftVersion")
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

  modLocalRuntime("me.djtheredstoner:DevAuth-fabric:1.2.2")

  modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
  modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

  implementation(kotlin("reflect"))
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

  shadowImplementation("com.googlecode.soundlibs:jlayer:1.0.1.4")
  shadowImplementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
  shadowImplementation("io.github.classgraph:classgraph:4.8.174")

  shadowImplementation("io.netty:netty-codec-socks:4.2.12.Final") {
    isTransitive = false
  }

  shadowImplementation("org.lwjgl:lwjgl-nanovg:$lwjglVersion")
  listOf("windows", "linux", "macos", "macos-arm64").forEach {
    shadowImplementation("org.lwjgl:lwjgl-nanovg:$lwjglVersion:natives-$it")
  }

  shadowImplementation("com.github.stivais:Commodore:$commodoreVersion")

  annotationProcessor("org.projectlombok:lombok:1.18.32")
  compileOnly("org.projectlombok:lombok:1.18.32")
}

loom {
  accessWidenerPath = file("src/main/resources/rsa.classtweaker")

  runConfigs.named("client") {
    isIdeConfigGenerated = true
    vmArgs.add("-Dmixin.debug.export=true")
    vmArgs.add("-Ddevauth.enabled=true")
    vmArgs.add("-Ddevauth.account=alt")
    vmArgs.add("-XX:+AllowEnhancedClassRedefinition")
    vmArgs.add("-XX:+IgnoreUnrecognizedVMOptions")
  }
}

afterEvaluate {
  loom.runs.named("client") {
    vmArg("-javaagent:${configurations.compileClasspath.get().find { it.name.contains("sponge-mixin") }}")
  }
}

tasks {
  withType<JavaCompile>().configureEach {
    options.release = 21
  }

  processResources {
    filesMatching("fabric.mod.json") {
      expand(project.properties)
    }
  }

  jar {
    archiveClassifier = "nodeps"
    destinationDirectory = layout.buildDirectory.dir("badjars")
  }

  shadowJar {
    archiveClassifier = "dev-shadow"
    destinationDirectory = layout.buildDirectory.dir("badjars")

    configurations = listOf(shadowImplementation)

    minimize()

    exclude("META-INF/maven/")
  }

  remapJar {
    archiveClassifier = null
    from(shadowJar)
    inputFile = shadowJar.get().archiveFile
  }
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_21
    freeCompilerArgs.add("-Xlambdas=class")
    freeCompilerArgs.add("-Xexplicit-backing-fields")
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}
