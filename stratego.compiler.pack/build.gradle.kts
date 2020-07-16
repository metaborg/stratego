plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform("org.metaborg:parent:$version"))

  api("org.metaborg:org.spoofax.terms:$version")
  api("org.metaborg:org.metaborg.util:$version")
  api("org.slf4j:slf4j-api")
  compileOnly("com.google.code.findbugs:jsr305")
}
