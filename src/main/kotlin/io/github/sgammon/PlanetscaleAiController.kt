package io.github.sgammon

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.serde.annotation.Serdeable

@Controller("/planetscaleAi")
class PlanetscaleAiController {
    companion object {
        const val sandboxDbName = "planetscale-ai-sample-1"
    }

    /** List of database names. */
    @Serdeable
    data class ListDatabaseNamesResponse(
        val databaseNames: List<String>,
    )


    @Get(uri="/listOfDatabasesByName", produces=[MediaType.APPLICATION_JSON])
    fun planetscaleDatabaseNamesList(): HttpResponse<ListDatabaseNamesResponse> {
        return HttpResponse.ok(ListDatabaseNamesResponse(
            listOf(sandboxDbName),
        ))
    }
}
