package io.github.sgammon

import io.micronaut.runtime.Micronaut.run
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server

@OpenAPIDefinition(
    info = Info(
        title = "Planetscale AI Agent API",
        version = "1.0",
    ),
    servers = [
        Server(
            url = "https://planetscale.ai",
            description = "Server endpoint",
        ),
    ],
)
object Api
fun main(args: Array<String>) {
    run(*args)
}
