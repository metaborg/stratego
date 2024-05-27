plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.devenv.spoofax.gradle.langspec")
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
