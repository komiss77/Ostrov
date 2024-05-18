plugins {
  `java-library`
  //`maven-publish`
  id("io.papermc.paperweight.userdev") version "1.5.11"
  id("xyz.jpenilla.run-paper") version "2.2.3"
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "ru.ostrov77"
version = "3.0"
description = "ostrov77"

dependencies {
  paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
  compileOnly(fileTree("libs"))// api(fileTree("libs"))
  compileOnly("com.velocitypowered:velocity-api:3.1.1")
  annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
  //implementation("redis.clients:jedis:4.3.1")
  api("redis.clients:jedis:4.3.1")
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
  // Configure reobfJar to run when invoking the build task
  assemble {
    dependsOn(reobfJar)
  }

  java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
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

  reobfJar {
    // This is an example of how you might change the output location for reobfJar. It's recommended not to do this
    // for a variety of reasons, however it's asked frequently enough that an example of how to do it is included here.
    outputJar.set(layout.buildDirectory.file("Ostrov.jar"))
  }


  //https://imperceptiblethoughts.com/shadow/configuration/dependencies/#embedding-jar-files-inside-your-shadow-jar
  shadowJar {
    dependencies {
      exclude(dependency("org.json:json:.*"))
      exclude(dependency("org.slf4j:slf4j-api:.*"))
      exclude(dependency("com.google.code.gson:gson:.*"))
    }
    relocate("redis.clients.jedis", "ru.komiss77.modules.redis.jedis")
    relocate("redis.clients.util", "ru.komiss77.modules.redis.jedisutil")
    //minimize()
  }
}
