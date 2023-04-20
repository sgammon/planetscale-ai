# planetscale ai

[![Elide](https://elide.dev/shield)](https://elide.dev)
[![GraalVM](https://img.shields.io/badge/GraalVM-22.3.x-blue.svg?logo=oracle)](https://www.graalvm.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.8.20-blue.svg?logo=kotlin)](http://kotlinlang.org)

it's a plugin for openAI that allows you to access your PlanetScale database. you can ask it stuff about your DB, or ask
it for optimization opportunities. we are working on making it dispatchable from github as well.

### how to use it

select the planetscale plugin in the openAI interface, and then write and submit a prompt. it will dispatch this bot,
which then dispatches OpenAI to generate a query from your natural language input. that query is then submitted to your
planetscale db, the results are interpreted, and returned to your convo.

when prompted for a domain for plugin discovery, enter:
```
planetscale.ai
```

### is it done yet

yes

### is there a blog post about it

[you bet](https://cacheflow.blog)

### how do i build it/contribute?

you can build the codebase like any regular Gradle Kotlin project, with:
```
./gradlew build
```

if you want to play with a native image, try:
```
./gradlew nativeCompile
```

### what is it built with?

- [Elide](https://elide.dev)
- [Buildless](https://less.build)
- [PlanetScale](https://planetscale.com)
- [Micronaut](https://micronaut.io)
- [Kotlin](https://kotlinlang.org)
- [Gradle](https://gradle.org)
- [GraalVM](https://graalvm.org)
