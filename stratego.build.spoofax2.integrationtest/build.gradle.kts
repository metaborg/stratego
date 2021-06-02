plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name:$version"
val spoofax2Version: String by ext
dependencies {
  api(platform("org.metaborg:parent:$spoofax2Version"))

  api(compositeBuild("stratego.build.spoofax2"))
  compileOnly("com.google.code.findbugs:jsr305")

  testImplementation("org.metaborg:pie.runtime")
  testImplementation("org.metaborg:strategoxt-jar:$spoofax2Version")
  testCompileOnly("com.google.code.findbugs:jsr305")
}
