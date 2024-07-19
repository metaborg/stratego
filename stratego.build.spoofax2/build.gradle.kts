plugins {
    `java-library`
    `maven-publish`
    id("org.metaborg.convention.java")
    id("org.metaborg.convention.maven-publish")
}

dependencies {
    api(platform(libs.metaborg.platform)) { version { require("latest.integration") } }

    api(project(":stratego.build"))

    api(libs.metaborg.core)
    api(libs.spoofax.core)

    api(libs.metaborg.pie.taskdefs.guice)

    implementation(libs.jakarta.annotation)
    implementation(libs.jakarta.inject)

    testImplementation(libs.junit)
}
