plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

dependencies {
  api(platform("org.metaborg:parent:$version"))

  api("org.metaborg:pie.taskdefs.guice")
  api("org.metaborg:pie.api")
  api("org.metaborg:org.metaborg.util:$version")
  api("org.metaborg:org.metaborg.core:$version")
  api("org.metaborg:org.metaborg.spoofax.core:$version")
  api("org.metaborg:org.spoofax.terms:$version")
  api("org.metaborg:org.spoofax.interpreter.core:$version")
  api("org.metaborg:org.strategoxt.strj:$version")
  api(project(":stratego.compiler.pack"))
  api("org.apache.commons:commons-lang3")
  api("org.apache.commons:commons-vfs2")
  api("commons-io:commons-io")
  api("com.google.guava:guava")
  compileOnly("com.google.code.findbugs:jsr305")
}
