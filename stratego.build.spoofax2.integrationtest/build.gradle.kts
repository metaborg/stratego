plugins {
    `java-library`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.convention.junit")
    id("org.metaborg.devenv.spoofax.gradle.base")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(libs.stratego.build.spoofax2)
    implementation(libs.jakarta.annotation)

    testImplementation(libs.metaborg.pie.runtime)
    testImplementation(libs.spoofax2.strategoxt.jar)
}

// Copy test resources into classes directory, to make them accessible as classloader resources at runtime.
val copyTestResourcesTask = tasks.create<Copy>("copyTestResources") {
    from("$projectDir/src/test/resources")
    into("$buildDir/classes/java/test")
}
tasks.getByName("processTestResources").dependsOn(copyTestResourcesTask)

// Additional dependencies which are injected into tests.
val strategoxtjarInjection = configurations.create("strategoxtjarInjection") {
    isTransitive = false
}
val spoofaxLanguageUsage = project.objects.named(Usage::class.java, "spoofax-language")
val strategoLangInjection = configurations.create("strategoLangInjection") {
    isTransitive = false
    attributes.attribute(Usage.USAGE_ATTRIBUTE, spoofaxLanguageUsage)
}
val oldStrategoLangInjection = configurations.create("oldStrategoLangInjection") {
    isTransitive = false
    attributes.attribute(Usage.USAGE_ATTRIBUTE, spoofaxLanguageUsage)
}
dependencies {
    strategoxtjarInjection(platform(libs.metaborg.platform))
    strategoLangInjection(platform(libs.metaborg.platform))
    oldStrategoLangInjection(platform(libs.metaborg.platform))
    strategoxtjarInjection(libs.spoofax2.strategoxt.jar)
    strategoLangInjection(project(":stratego.lang"))
    oldStrategoLangInjection(project(":org.metaborg.meta.lang.stratego"))
}
tasks.test {
    // Pass injections to tests in the form of system properties
    dependsOn(strategoxtjarInjection, strategoLangInjection, oldStrategoLangInjection)
    doFirst {
        // Wrap in doFirst to properly defer dependency resolution to the task execution phase.
        systemProperty("strategoxt-jar", strategoxtjarInjection.resolvedConfiguration.resolvedArtifacts.first().file)
        systemProperty("stratego-lang", strategoLangInjection.resolvedConfiguration.resolvedArtifacts.first().file)
        systemProperty("omml-stratego", oldStrategoLangInjection.resolvedConfiguration.resolvedArtifacts.first().file)
    }
}
