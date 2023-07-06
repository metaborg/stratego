plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.devenv.spoofax.gradle.langspec")
  id("de.set.ecj") // Use ECJ to speed up compilation of Stratego's generated Java files.
  `maven-publish`
}

// Replace source dependencies with overridden/local ones.
fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2BaselineVersion: String by ext
val spoofax2Version: String by ext
spoofaxLanguageSpecification {
  addCompileDependenciesFromMetaborgYaml.set(false)
  addSourceDependenciesFromMetaborgYaml.set(false)
}
dependencies {
  compileLanguage(compositeBuild("org.metaborg.meta.lang.esv"))

  sourceLanguage(compositeBuild("meta.lib.spoofax"))
}

metaborg { // Do not create Java publication; this project is already published as a Spoofax 2 language.
  javaCreatePublication = false
  javaCreateSourcesJar = false
  javaCreateJavadocJar = false
}

ecj {
  toolVersion = "3.21.0"
}
tasks.withType<JavaCompile> { // ECJ does not support headerOutputDirectory (-h argument).
  options.headerOutputDirectory.convention(provider { null })
}
