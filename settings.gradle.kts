/*  ПРОБЛЕМКИ
1) при запуске gradlew.bat: java.lang.UnsupportedClassVersionError: io/papermc/paperweight/userdev/PaperweightUser has been compiled by a
more recent version of the Java Runtime (class file version 55.0), this version of the Java Runtime only recognizes class
 file versions up to 52.0
 РЕШЕНИЕ : изменить gradle.properties файл в каталоге .gradle в HOME_DIRECTORY
org.gradle.java.home=C:/Program Files/Java/jdk-19
2) в сборке только META-INF : нехер перетаскивать сорцы из main/java, верни всё взад
 */

//pluginManagement {
    //repositories {
    //    gradlePluginPortal()
     //   maven("https://repo.papermc.io/repository/maven-public/");
        //flatDir { dirs 'D:/Docum/NetBeansProjects/Ostrov/dist/Ostrov.jar' }
        //flatDir {
        //  dirs 'libs'
        //}
   // }
//}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "Ostrov"
