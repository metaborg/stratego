plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.devenv.spoofax.gradle.langspec")
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
    compileLanguage(libs.spoofax2.esv.lang)     // Bootstrap using Spoofax 2 artifact
    compileLanguage(libs.spoofax2.sdf3.lang)    // Bootstrap using Spoofax 2 artifact
    compileLanguage(project(":stratego.lang"))

    sourceLanguage(project(":strategolib"))
    sourceLanguage(project(":stratego.lang"))
    sourceLanguage(project(":org.metaborg.meta.lang.stratego"))

    compileOnly(project(":strategolib"))
    compileOnly(libs.spoofax2.core)
}

metaborg {
    // Do not create Java publication; this project is already published as a Spoofax 2 language.
    javaCreatePublication = false
    javaCreateSourcesJar = false
    javaCreateJavadocJar = false
}
