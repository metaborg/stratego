plugins {
    id("org.metaborg.gradle.config.java-library")
    id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2Version: String by ext
dependencies {
    api(platform("org.metaborg:parent:$spoofax2Version"))

    api(project(":stratego.build"))

    api(compositeBuild("org.metaborg.core"))
    api(compositeBuild("org.metaborg.spoofax.core"))

    api("org.metaborg:pie.taskdefs.guice")

    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("jakarta.inject:jakarta.inject-api")
}
