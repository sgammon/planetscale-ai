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
