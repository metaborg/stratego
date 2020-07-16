rootProject.name = "stratego.root"

pluginManagement {
  repositories {
    maven("https://artifacts.metaborg.org/content/groups/public/")
  }
}

if(org.gradle.util.VersionNumber.parse(gradle.gradleVersion).major < 6) {
  enableFeaturePreview("GRADLE_METADATA")
}

include("org.metaborg.meta.lang.stratego")
include("stratego.build")
include("stratego.build.spoofax2")
include("stratego.build.spoofax3")
include("stratego.compiler.pack")
