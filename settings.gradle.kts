rootProject.name = "stratego-project"

// This is needed to let Gradle find the dependencies of the spoofax2-gradle plugin
pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://artifacts.metaborg.org/content/groups/public/")
    }

    versionCatalogs {
        create("libs") {
            from("dev.spoofax:spoofax3-catalog:0.0.0-SNAPSHOT")
        }
    }
}

//include("gpp")
//include("org.metaborg.meta.lang.stratego")
//include("stratego.build")
//include("stratego.build.spoofax2")
//include("stratego.lang")
//include("strategolib")

