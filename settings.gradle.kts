pluginManagement {
  repositories {
    maven("https://maven.fabricmc.net/")
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
  }

  plugins {
    id("net.fabricmc.fabric-loom-remap") version providers.gradleProperty("loomVersion")
  }
}

rootProject.name = "rsa"
