plugins {
  id("org.metaborg.gradle.config.java-library")
}

fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2Version: String by ext
dependencies {
  api(platform("org.metaborg:parent:$spoofax2Version"))

  api(compositeBuild("org.spoofax.terms"))
  api(compositeBuild("org.metaborg.util"))
  api("org.slf4j:slf4j-api")
  compileOnly("com.google.code.findbugs:jsr305")
}
