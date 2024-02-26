
// Normally, when you execute a task such as `test` in a multi-project build, you will execute
//  all `:test` tasks in all projects. In contrast, when you specifically execute `:test`
//  (prefixed with a colon), you execute the `:test` task only in the root project.
// Now, we would like to create a task in this composite build `testAll` that executes basically
//  the equivalent of `test` in each of the included multi-project builds, which would execute
//  `:test` in each of the projects. However, this seems to be impossible to write down. Instead,
//  we call the root `:test` task in the included build, and in each included build's multi-project
//  root project we'll extend the `test` task to depend on the `:test` tasks of the subprojects.

// Build tasks
tasks.register("assemble") {
    group = "Build"
    description = "Assembles the outputs of the subprojects and included builds."
    dependsOn(gradle.includedBuilds.map { it.task(":assemble") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("assemble") })
}
tasks.register("build") {
    group = "Build"
    description = "Assembles and tests the subprojects and included builds."
    dependsOn(gradle.includedBuilds.map { it.task(":build") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("build") })
}
tasks.register("clean") {
    group = "Build"
    description = "Cleans the outputs of the subprojects and included builds."
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("clean") })
}

// Publishing tasks
tasks.register("publish") {
    group = "Publishing"
    description = "Publishes all subprojects and included builds to a remote Maven repository."
    dependsOn(gradle.includedBuilds.map { it.task(":publish") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("publish") })
}
tasks.register("publishToMavenLocal") {
    group = "Publishing"
    description = "Publishes all subprojects and included builds to the local Maven repository."
    dependsOn(gradle.includedBuilds.map { it.task(":publishToMavenLocal") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("publishToMavenLocal") })
}

// Verification tasks
tasks.register("check") {
    group = "Verification"
    description = "Runs all checks on the subprojects and included builds."
    dependsOn(gradle.includedBuilds.map { it.task(":check") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("check") })
}
tasks.register("test") {
    group = "Verification"
    description = "Runs all unit tests on the subprojects and included builds."
    dependsOn(gradle.includedBuilds.map { it.task(":test") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("test") })
}

// Help tasks
tasks.register("allTasks") {
    group = "Help"
    description = "Displays all tasks of subprojects and included builds."
    dependsOn(gradle.includedBuilds.map { it.task(":tasks") })
    dependsOn(project.subprojects.mapNotNull { it.tasks.findByName("tasks") })
}