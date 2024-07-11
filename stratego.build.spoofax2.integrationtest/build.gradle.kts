plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
    id("org.metaborg.devenv.spoofax.gradle.base")
}

fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2Version: String by ext
dependencies {
    api(platform("org.metaborg:parent:$spoofax2Version"))

    api(compositeBuild("stratego.build.spoofax2"))
    implementation("jakarta.annotation:jakarta.annotation-api")

    testImplementation("org.metaborg:pie.runtime")
    testImplementation("org.metaborg:strategoxt-jar:$spoofax2Version")
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
    strategoxtjarInjection(platform("org.metaborg:parent:$spoofax2Version"))
    strategoLangInjection(platform("org.metaborg:parent:$spoofax2Version"))
    oldStrategoLangInjection(platform("org.metaborg:parent:$spoofax2Version"))
    strategoxtjarInjection("org.metaborg:strategoxt-jar:$spoofax2Version")
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
