plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2Version: String by ext
val pieVersion = "0.19.7" // HACK: override PIE version to make it binary compatible with this version.
dependencies {
  api(platform("org.metaborg:parent:$spoofax2Version"))

  api("org.metaborg:pie.api:$pieVersion")
  api("org.metaborg:pie.task.archive:$pieVersion")
  api("org.metaborg:resource")
  api("org.metaborg:org.metaborg.util:$spoofax2Version")
  api(compositeBuild("org.spoofax.terms"))
  api(compositeBuild("org.spoofax.interpreter.core"))
  api(compositeBuild("org.strategoxt.strj"))
  implementation("org.apache.commons:commons-lang3")
  api("commons-io:commons-io")
  implementation("jakarta.annotation:jakarta.annotation-api")
  implementation("jakarta.inject:jakarta.inject-api")
  implementation("javax.inject:javax.inject")
}
