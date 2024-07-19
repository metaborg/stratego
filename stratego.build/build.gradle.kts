plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
    id("org.metaborg.convention.junit")
}

val pieVersion = "0.19.7" // HACK: override PIE version to make it binary compatible with this version.
dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(libs.metaborg.pie.api) { version { require(pieVersion) } }
    api(libs.metaborg.pie.task.archive) { version { require(pieVersion) } }
    api(libs.metaborg.resource.api)
    implementation(libs.metaborg.util)
    api(libs.spoofax.terms)
    api(libs.jsglr.shared)
    api(libs.jsglr2)
    api(libs.interpreter.core)
    api(libs.strategoxt.strj)
    implementation(libs.commons.lang3)
    implementation(libs.commons.io)
    implementation(libs.jakarta.annotation)
    implementation(libs.jakarta.inject)
    implementation(libs.jakarta.annotation)
}
