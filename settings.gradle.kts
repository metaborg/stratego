rootProject.name = "stratego"

pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

enableFeaturePreview("GRADLE_METADATA")

include("org.metaborg.meta.lang.stratego")
include("stratego.build")
include("stratego.compiler.pack")

