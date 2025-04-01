

// for Ant filter
import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
  `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
//    id("io.papermc.paperweight.userdev") version "2.0.0-beta.1"
    //id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "ru.ostrov77"
version = "3.1.21.4"
description = "ostrov77"

dependencies {
  paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
  compileOnly(fileTree("libs"))
  compileOnly("com.velocitypowered:velocity-api:3.1.1")
  annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
}

repositories {
  maven {
    name = "papermc"
    url = uri("https://repo.papermc.io/repository/maven-public/")
  }
  mavenCentral()
}

sourceSets {
  main {
    java {
      srcDir("src/")
    }
    resources {
      srcDir("resources/")
    }
  }
}

tasks {
  // Configure ArtifactConfiguration to be MOJANG_PRODUCTION
  build {
    paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
  }

  java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
  }

  compileJava {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
    // See https://openjdk.java.net/jeps/247 for more information.
    //options.release.set(17)
  }

  javadoc {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
  }

  /*reobfJar {
    // This is an example of how you might change the output location for reobfJar. It's recommended not to do this
    // for a variety of reasons, however it's asked frequently enough that an example of how to do it is included here.
    outputJar.set(layout.buildDirectory.file("Ostrov.jar"))
  }*/

  jar {
      //from(zipTree("libs/jedis-4.3.1.zip"))
    destinationDirectory.set(layout.buildDirectory)
    archiveFileName.set("Ostrov.jar")
  }
}
