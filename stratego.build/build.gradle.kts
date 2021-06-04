plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2Version: String by ext
dependencies {
  api(platform("org.metaborg:parent:$spoofax2Version"))

  api("org.metaborg:pie.api")
  api("org.metaborg:resource")
  implementation(compositeBuild("org.metaborg.util"))
  api(compositeBuild("org.spoofax.terms"))
  api(compositeBuild("org.spoofax.interpreter.core"))
  api(compositeBuild("org.strategoxt.strj"))
  api(project(":stratego.compiler.pack"))
  implementation("org.apache.commons:commons-lang3")
  implementation("commons-io:commons-io")
  compileOnly("com.google.code.findbugs:jsr305")
}
