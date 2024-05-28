plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.devenv.spoofax.gradle.langspec")
    `maven-publish`
}

// Replace language dependencies with overridden/local ones.
fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2BaselineVersion: String by ext
val spoofax2Version: String by ext
spoofaxLanguageSpecification {
    addCompileDependenciesFromMetaborgYaml.set(false)
    addSourceDependenciesFromMetaborgYaml.set(false)
    // Ignore trans/stratego-box.tbl, as an input, as it is created by the build.
    spoofaxBuildApproximateAdditionalInputExcludePatterns.add("**/stratego-box.tbl")
}
dependencies {
    compileLanguage(compositeBuild("org.metaborg.meta.lang.esv"))
    compileLanguage(compositeBuild("org.metaborg.meta.lang.template"))
    compileLanguage(project(":stratego.lang"))

    sourceLanguage(project(":strategolib"))
    sourceLanguage(project(":stratego.lang"))
    sourceLanguage(project(":org.metaborg.meta.lang.stratego"))

    compileOnly(project(":strategolib"))
}

metaborg {
    // Do not create Java publication; this project is already published as a Spoofax 2 language.
    javaCreatePublication = false
    javaCreateSourcesJar = false
    javaCreateJavadocJar = false
}
