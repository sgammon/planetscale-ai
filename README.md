# PlanetScale AI

[![Elide](https://elide.dev/shield)](https://elide.dev)
[![GraalVM](https://img.shields.io/badge/GraalVM-22.3.x-blue.svg?logo=oracle)](https://www.graalvm.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.8.20-blue.svg?logo=kotlin)](http://kotlinlang.org)

It's a plugin for OpenAI that allows you to access your PlanetScale database. You can ask it about your DB, or ask
it for optimization opportunities. Ee are working on making it dispatchable from github as well.

### How to use it

Select the PlanetScale plugin in the OpenAI plugins interface, and then write and submit a prompt. It will dispatch this bot,
which then dispatches OpenAI to generate a query from your natural language input. That query is then submitted to your
PlanetScale DB, the results are interpreted, and returned to your convo in plain english.

When prompted for a domain for plugin discovery, enter:
```
planetscale.ai
```

### Is it done yet

Yes

### Is there a blog post about it

[You bet!](https://cacheflow.blog)

### How do I build it/contribute?

You can build the codebase like any regular Gradle Kotlin project, with:
```
./gradlew build
```

If you want to play with a native image, try:
```
./gradlew nativeCompile
```

### What is it built with?

- [Elide](https://elide.dev)
- [Buildless](https://less.build)
- [PlanetScale](https://planetscale.com)
- [CloudFlare Workers](https://workers.cloudflare.com/)
- [Micronaut](https://micronaut.io)
- [Kotlin](https://kotlinlang.org)
- [Gradle](https://gradle.org)
- [GraalVM](https://graalvm.org)

### What can I do with Gradle?

Run `./gradlew tasks` to find out:

<details>
  <summary>Output example</summary>

```
> Task :tasks

------------------------------------------------------------
Tasks runnable from root project 'planetscale-ai'
------------------------------------------------------------

Application tasks
-----------------
run - Runs this project as a JVM application
runShadow - Runs this project as a JVM application using the shadow jar
startShadowScripts - Creates OS specific scripts to run the project as a JVM application using the shadow jar

Build tasks
-----------
assemble - Assembles the outputs of this project.
build - Assembles and tests this project.
buildDependents - Assembles and tests this project and all projects that depend on it.
buildJs - Build JS targets via Node/NPM
buildKotlinToolingMetadata - Build metadata json file containing information about the used Kotlin tooling
buildLayers - Builds application layers for use in a Docker container (main image)
buildNativeLayersTask - Builds application layers for use in a Docker container (main image)
buildNeeded - Assembles and tests this project and all projects it depends on.
classes - Assembles main classes.
clean - Deletes the build directory.
collectReachabilityMetadata - Obtains native reachability metdata for the runtime classpath configuration
dockerBuild - Builds a Docker Image (image main)
dockerBuildNative - Builds a Native Docker Image using GraalVM (image main)
dockerfile - Builds a Docker File for image main
dockerfileNative - Builds a Native Docker File for image main
inspectRuntimeClasspath - Performs sanity checks of the runtime classpath to warn about misconfigured builds
jar - Assembles a jar archive containing the main classes.
kotlinSourcesJar - Assembles a jar archive containing the sources of target 'kotlin'.
metadataCopy - Copies metadata collected from tasks instrumented with the agent into target directories.
nativeCompile - Compiles a native image for the main binary
nativeRun - Executes the main native binary
nativeTestCompile - Compiles a native image for the test binary
testClasses - Assembles test classes.
testResourcesClasses - Assembles test resources classes.

Build Setup tasks
-----------------
init - Initializes a new Gradle build.
wrapper - Generates Gradle wrapper files.

Distribution tasks
------------------
assembleDist - Assembles the main distributions
assembleShadowDist - Assembles the shadow distributions
distTar - Bundles the project as a distribution.
distZip - Bundles the project as a distribution.
installDist - Installs the project as a distribution as-is.
installShadowDist - Installs the project as a distribution as-is.
shadowDistTar - Bundles the project as a distribution.
shadowDistZip - Bundles the project as a distribution.

Documentation tasks
-------------------
javadoc - Generates Javadoc API documentation for the main source code.

Gradle Enterprise tasks
-----------------------
buildScanPublishPrevious - Publishes the data captured by the last build.
provisionGradleEnterpriseAccessKey - Provisions a new access key for this build environment.

Help tasks
----------
buildEnvironment - Displays all buildscript dependencies declared in root project 'planetscale-ai'.
dependencies - Displays all dependencies declared in root project 'planetscale-ai'.
dependencyInsight - Displays the insight into a specific dependency in root project 'planetscale-ai'.
help - Displays a help message.
javaToolchains - Displays the detected java toolchains.
kotlinDslAccessorsReport - Prints the Kotlin code for accessing the currently available project extensions and conventions.
outgoingVariants - Displays the outgoing variants of root project 'planetscale-ai'.
projects - Displays the sub-projects of root project 'planetscale-ai'.
properties - Displays the properties of root project 'planetscale-ai'.
resolvableConfigurations - Displays the configurations that can be resolved in root project 'planetscale-ai'.
tasks - Displays the tasks runnable from root project 'planetscale-ai'.

IDE tasks
---------
cleanEclipse - Cleans all Eclipse files.
eclipse - Generates all Eclipse files.

Jib tasks
---------
jib - Builds a container image to a registry.
jibBuildTar - Builds a container image to a tarball.
jibDockerBuild - Builds a container image to a Docker daemon.

Micronaut Test Resources tasks
------------------------------
internalStartTestResourcesService - Starts the test resources server
startTestResourcesService - Starts the test resources server in standalone mode
stopTestResourcesService - Stops the test resources server

Node tasks
----------
nodeSetup - Download and install a local node/npm version.

Npm tasks
---------
npmInstall - Install node packages from package.json.
npmSetup - Setup a specific version of npm to be used by the build.

Pnpm tasks
----------
pnpmInstall - Install node packages from package.json.
pnpmSetup - Setup a specific version of pnpm to be used by the build.

Publish tasks
-------------
publishWorkersLive - Publish CloudFlare Workers to live environments
publishWorkersStaging - Publish CloudFlare Workers to staging environments

Shadow tasks
------------
knows - Do you know who knows?
shadowJar - Create a combined JAR of project and runtime dependencies

Upload tasks
------------
dockerPush - Pushes the main Docker Image
dockerPushNative - Pushes a Native Docker Image using GraalVM (image main)

Verification tasks
------------------
check - Runs all checks.
nativeTest - Executes the test native binary
test - Runs the test suite.

Yarn tasks
----------
yarn - Install node packages using Yarn.
yarnSetup - Setup a specific version of Yarn to be used by the build.
```
</details>
