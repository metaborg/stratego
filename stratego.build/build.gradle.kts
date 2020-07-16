plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(platform("org.metaborg:parent:$version"))

  api("org.metaborg:pie.api")
  implementation("org.metaborg:org.metaborg.util:$version")
  api("org.metaborg:org.spoofax.terms:$version")
  api("org.metaborg:org.spoofax.interpreter.core:$version")
  api("org.metaborg:org.strategoxt.strj:$version")
  api(project(":stratego.compiler.pack"))
  implementation("org.apache.commons:commons-lang3")
  implementation("commons-io:commons-io")
  compileOnly("com.google.code.findbugs:jsr305")
}
