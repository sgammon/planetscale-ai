package io.github.sgammon

import com.theokanning.openai.completion.CompletionRequest
import com.theokanning.openai.service.OpenAiService
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.QueryValue
import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import javax.transaction.Transactional

@Transactional
@Controller("/planetscaleAi")
open class PlanetscaleAiController {
    companion object {
        // Database name to connect to at Planetscale.
        private const val sandboxDbName = "planetscale-ai-sample-1"

        // Sample database containing company data.
        private const val sampleCompanyDbName = "employees"

        // AI model to use via OpenAI when translating natural language to queries.
        private const val aiModelToUse = "text-davinci-003"

        // IMDB table names.
        private val imdbTables = listOf(
            "Alias_attributes",
            "Alias_types",
            "Aliases",
            "Directors",
            "Episode_belongs_to",
            "Had_role",
            "Known_for",
            "Name_worked_as",
            "Names_",
            "Principals",
            "Title_genres",
            "Title_ratings",
            "Titles",
            "Writers",
        )
    }

    // Logger.
    private val logging: Logger = LoggerFactory.getLogger("planetscale-ai")

    // Active SQL connection.
    @Inject private lateinit var connection: Connection

    /** API key to use for Open AI calls. */
    private val apiKey = System.getenv("OPENAI_API_KEY")?.ifBlank { null } ?: error(
        "Failed to resolve API key"
    )

    /** Service client for talking to Open AI. */
    private val service = OpenAiService(apiKey)

    /** List of database names. */
    @Serdeable data class ListDatabaseNamesResponse(
        val databaseNames: List<String>,
    )

    /** List of table names. */
    @Serdeable data class ListTableNamesResponse(
        val databaseName: String,
        val tableNames: List<String>,
    )

    /** Response to a natural language SQL query. */
    @Serdeable data class NaturalLanguageQueryResponse(
        val query: String,
        val results: List<Map<String, Any>>,
        val columns: List<String>? = null,
    )

    /** Enumeration which lists each type that a column can inhabit in MySQL. */
    @Serdeable enum class ColumnType {
        /** String or text column. */
        STRING,

        /** Numeric column. */
        NUMBER,

        /** Boolean column. */
        BOOLEAN,

        /** Date column. */
        DATE,

        /** Datetime column. */
        DATETIME,

        /** Timestamp column. */
        TIMESTAMP;

        companion object {
            /** @return Cleaned up simple type name from a MySQL column definition. */
            private fun cleanupTypeName(name: String): String {
                // remove any parentheses and parameters (e.g. `varchar(255)` -> `varchar`)
                val parenIndex = name.indexOf('(')
                return if (parenIndex == -1) name else name.substring(0, parenIndex)
            }

            /** @return [ColumnType] for the [typeName] of a MySQL column type. */
            internal fun fromMySqlType(typeName: String) = when (cleanupTypeName(typeName)) {
                "varchar", "char", "text", "mediumtext", "longtext" -> STRING
                "int", "tinyint", "smallint", "mediumint", "bigint", "float", "double", "decimal" -> NUMBER
                "boolean", "bit" -> BOOLEAN
                "date" -> DATE
                "datetime" -> DATETIME
                "timestamp" -> TIMESTAMP
                else -> error("Unknown column type: $typeName")
            }
        }
    }

    /** Describes the structure/schema of a single table column. */
    @Serdeable data class ColumnSchema(
        val name: String,
        val type: ColumnType,
    )

    /** Structure which describes a table schema. */
    @Serdeable data class TableSchemaResponse(
        val name: String,
        val columns: List<ColumnSchema>,
        val primaryKey: String? = null,
    )

    /** Response indicating a query translation error occurred. */
    @Serdeable data class FailedToTranslateQueryError(
        val errorText: String,
    )

    /** Error indicating that we don't know what DB/table to query. */
    @Serdeable data class DisambiguationError(
        val neededInput: String,
        val errorText: String,
    )

    /** Error thrown when no databases are available at all. */
    @Serdeable data class NoDatabasesAvailableError(
        val errorText: String,
    )

    /**
     * List the user's databases in Planetscale.
     *
     * @reteurn List of database names (see [ListDatabaseNamesResponse]).
     */
    @Get(uri="/listOfDatabasesByName", produces=[MediaType.APPLICATION_JSON])
    open fun planetscaleDatabaseNamesList(): ListDatabaseNamesResponse? {
        return ListDatabaseNamesResponse(listOf(
            sandboxDbName,
            sampleCompanyDbName,
        ))
    }

    /**
     *  List tables at the database named [databaseName] in Planetscale.
     *
     * @param databaseName Name of the database to list tables in.
     * @return List of table names (see [ListTableNamesResponse]).
     */
    @Get(uri="/listTablesForDatabaseByName", produces=[MediaType.APPLICATION_JSON])
    open fun planetscaleTableNamesList(
        @QueryValue("databaseName") databaseName: String,
    ): ListTableNamesResponse? {
        return ListTableNamesResponse(
            databaseName = databaseName,
            tableNames = listOf("categories", "products").plus(imdbTables).sorted(),
        )
    }

    /**
     * Given a [databaseName] and [tableName], return the schema for the table from the user's Planetscale database.
     *
     * @param databaseName Name of the database.
     * @param tableName Name of the table schema to fetch.
     * @return HTTP response containing JSON which describes the table.
     */
    @Get(uri="/tableSchemaByName", produces=[MediaType.APPLICATION_JSON])
    open fun planetscaleTableSchemaByName(
        @QueryValue("databaseName") databaseName: String,
        @QueryValue("tableName") tableName: String,
    ): HttpResponse<TableSchemaResponse> {
        // fetch the primary key for the table
        val primaryKey = connection.createStatement().use { statement ->
            statement.executeQuery("SHOW KEYS FROM $tableName WHERE Key_name = 'PRIMARY'").use { resultSet ->
                if (resultSet.next()) {
                    resultSet.getString("Column_name")
                } else {
                    null
                }
            }
        }

        // obtain a connection to the current database, and use it to introspect the schema
        // at the table named `tableName`. use the introspected schema to build an object of
        // type `TableSchemaResponse`.
        return HttpResponse.ok(connection.createStatement().use { statement ->
            statement.executeQuery("DESCRIBE $tableName").use { resultSet ->
                val columns = mutableListOf<ColumnSchema>()
                while (resultSet.next()) {
                    val name = resultSet.getString("Field")
                    val type = ColumnType.fromMySqlType(resultSet.getString("Type"))
                    columns.add(ColumnSchema(
                        name,
                        type,))
                }
                TableSchemaResponse(
                    tableName,
                    columns,
                    primaryKey,
                )
            }
        })
    }

    /**
     * Accepts a natural language prompt, translates the prompt to an SQL query, and
     * runs the query against the user's database in read-only form; then, returns the
     * results to the calling user for summarization.
     *
     * @param databaseName Optional database name; can be withheld if there is only one
     *   database to query. Databases can be listed via [planetscaleDatabaseNamesList].
     * @param naturalLanguage Natural language prompt to translate to SQL.
     * @return Results of the executed SQL query against the database.
     */
    @Get(uri="/naturalLanguageSQLQuery", produces=[MediaType.APPLICATION_JSON])
    @Operation(
        summary = "Accepts a natural language prompt, translates the prompt to an SQL query, and runs the query; returns the results.",
        description = """
            Accepts a natural language prompt, translates it to an SQL query, and runs the query against the user's database in read-only form; then, returns the results to the calling user for summarization.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response indicating columns and rows of a resultset from the query",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = NaturalLanguageQueryResponse::class),
                    )
                ]
            )
        ]
    )
    open fun naturalLanguageQuery(
        @QueryValue("databaseName") databaseName: String?,
        @QueryValue("naturalLanguage") naturalLanguage: String,
    ): HttpResponse<*> {
        val databases = planetscaleDatabaseNamesList()?.databaseNames
            ?: error("Failed to resolve database names")

        // if there is more than one database, fail
        when {
            databases.size > 1 -> return HttpResponse.badRequest(DisambiguationError(
                neededInput = "databaseName",
                errorText = "Please specify a database name",
            ))

            databases.isEmpty() -> return HttpResponse.badRequest(NoDatabasesAvailableError(
                errorText = "No databases available to query",
            ))
        }

        // extract db name
        val dbname = databaseName ?: databases.first()

        // generate the prompt
        val prompt = """
### Turn this natural language prompt into a SQL query
#
# We are generating MySQL SQL dialect queries. You should only generate read-only queries.
#
# The user may have specified a database name, or not. The user may use colloquial names for their tables, so make sure
# to use the table list below to look for names (unless they use an exact name matching a table, in which case, use that
# name).
#
# Make sure to only use tables that actually exist in the database. Here is the list of active tables. Also
# keep in mind that the user may use colloquial names from their columns, so make sure to only reference
# columns which exist. If a table exists with different capitalization than an input, it's safe to just
# assume that is the table the user is talking about.
#
# Tables in the database:
${planetscaleTableNamesList(dbname)?.tableNames?.joinToString("\n") { "# - $it" } ?: error("No available tables")}
#
$naturalLanguage
        """.trimIndent()

        logging.info("Rendered prompt:\n\n$prompt")

        // fire off a request to the API
        val result = service.createCompletion(CompletionRequest.builder()
            .model(aiModelToUse)
            .stop(listOf("#", ";"))
            .prompt(prompt)
            .temperature(0.1)
            .maxTokens(1_000)
            .build())

        val rawQuery = result.choices.first().text

        // static query
        val sanitized = rawQuery.trim().replace("\n", "")
        val query = if (sanitized.startsWith("?")) sanitized.drop(1).trim() else sanitized
        logging.info("Executing query: '$query'")
        val statement = connection.createStatement()
        val err = statement.execute(query)
        if (!err) {
            logging.error("Failed to execute database query")
            return HttpResponse.badRequest(FailedToTranslateQueryError(
                errorText = "Failed to execute database query. Please try again",
            ))
        } else {
            // obtain the JDBC resultset, and sniff it to determine the structure of the results; produce a list of
            // columns, and a list of rows.
            val resultSet = statement.resultSet
            val columns = resultSet.metaData.columnCount
            val rows = mutableListOf<List<String>>()
            while (resultSet.next()) {
                val row = mutableListOf<String>()
                for (i in 1..columns) {
                    row.add(resultSet.getString(i))
                }
                rows.add(row)
            }

            // with the above resultset, obtain the proper column names to use, and then re-format the set of results
            // as a list of maps, with each map keyed by column name.
            val columnNames = (1..columns).map { resultSet.metaData.getColumnName(it) }
            val results = rows.map { row ->
                columnNames.zip(row).toMap()
            }

            return HttpResponse.ok(NaturalLanguageQueryResponse(
                query = query,
                results = results,
            ))
        }
    }
}
