plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.devenv.spoofax.gradle.langspec")
}

spoofaxLanguageSpecification {
    addCompileDependenciesFromMetaborgYaml.set(false)
    addSourceDependenciesFromMetaborgYaml.set(false)
    // Ignore trans/stratego-box.tbl, as an input, as it is created by the build.
    spoofaxBuildApproximateAdditionalInputExcludePatterns.add("**/stratego-box.tbl")

    // We add the dependency manually and don't change the repositories
    // Eventually, this functionality should be removed from spoofax.gradle
    addSpoofaxCoreDependency.set(false)
    addSpoofaxRepository.set(false)
}
dependencies {
    compileLanguage(libs.spoofax2.esv.lang)     // Bootstrap using Spoofax 2 artifact
    compileLanguage(libs.spoofax2.sdf3.lang)    // Bootstrap using Spoofax 2 artifact
    compileLanguage(project(":stratego.lang"))

    sourceLanguage(project(":strategolib"))
    sourceLanguage(project(":stratego.lang"))
    sourceLanguage(project(":org.metaborg.meta.lang.stratego"))

    compileOnly(project(":strategolib"))
    compileOnly(libs.spoofax.core)
}

