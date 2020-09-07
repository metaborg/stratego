plugins {
  id("org.metaborg.gradle.config.root-project") version "0.3.21"
  id("org.metaborg.gitonium") version "0.1.3"

  // Set versions for plugins to use, only applying them in subprojects (apply false here).
  id("org.metaborg.spoofax.gradle.langspec") version "0.4.2" apply false
  id("de.set.ecj") version "1.4.1" apply false
}

subprojects {
  metaborg {
    configureSubProject()
  }
}

allprojects {
  // Override version from gitonium, as Spoofax Core uses a different versioning scheme.
  // Needs to be kept in sync with metaborgVersion of Spoofax 3 and theSpoofax 2 Gradle plugin.
  version = "2.5.10"
}
