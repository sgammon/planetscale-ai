package io.github.sgammon

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.MediaType
import java.net.URI


@Controller
class RedirectController {
    @Get("/")
    fun redirect(): HttpResponse<Any> = HttpResponse.temporaryRedirect(URI.create("https://planetscale.com/"));
}