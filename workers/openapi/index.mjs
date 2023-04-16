
const descriptor = {
    "openapi": "3.0.1",
    "info": {
        "title": "Planetscale AI Agent API",
        "version": "1.0"
    },
    "servers": [
        {
            "url": "https://planetscale-ai-api.elide.dev",
            "description": "Server endpoint"
        }
    ],
    "paths": {
        "/planetscaleAi/listOfDatabasesByName": {
            "get": {
                "summary": "List the user's databases in Planetscale.",
                "description": "List the user's databases in Planetscale.",
                "operationId": "planetscaleDatabaseNamesList",
                "responses": {
                    "200": {
                        "description": "planetscaleDatabaseNamesList 200 response",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/PlanetscaleAiController.ListDatabaseNamesResponse"
                                }
                            }
                        }
                    }
                }
            }
        },
        "/planetscaleAi/listTablesForDatabaseByName": {
            "get": {
                "summary": "List tables at the database named \\[databaseName\\] in Planetscale.",
                "description": "List tables at the database named \\[databaseName\\] in Planetscale.",
                "operationId": "planetscaleTableNamesList",
                "parameters": [
                    {
                        "name": "databaseName",
                        "in": "query",
                        "description": "Name of the database to list tables in.",
                        "required": true,
                        "schema": {
                            "type": "string"
                        }
                    }
                ],
                "responses": {
                    "200": {
                        "description": "List of table names (see \\[ListTableNamesResponse\\]).",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/PlanetscaleAiController.ListTableNamesResponse"
                                }
                            }
                        }
                    }
                }
            }
        },
        "/planetscaleAi/naturalLanguageSQLQuery": {
            "get": {
                "summary": "Accepts a natural language prompt, translates the prompt to an SQL query, and runs the query; returns the results.",
                "description": "\n            Accepts a natural language prompt, translates it to an SQL query, and runs the query against the user's database in read-only form; then, returns the results to the calling user for summarization.\n        ",
                "operationId": "naturalLanguageQuery",
                "parameters": [
                    {
                        "name": "databaseName",
                        "in": "query",
                        "description": "Optional database name; can be withheld if there is only one database to query. Databases can be listed via \\[planetscaleDatabaseNamesList\\].",
                        "schema": {
                            "type": "string",
                            "nullable": true
                        }
                    },
                    {
                        "name": "naturalLanguage",
                        "in": "query",
                        "description": "Natural language prompt to translate to SQL.",
                        "required": true,
                        "schema": {
                            "type": "string"
                        }
                    }
                ],
                "responses": {
                    "200": {
                        "description": "Successful response indicating columns and rows of a resultset from the query",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/PlanetscaleAiController.NaturalLanguageQueryResponse"
                                }
                            }
                        }
                    }
                }
            }
        },
        "/planetscaleAi/tableSchemaByName": {
            "get": {
                "summary": "Given a \\[databaseName\\] and \\[tableName\\], return the schema for the table from the user's Planetscale database.",
                "description": "Given a \\[databaseName\\] and \\[tableName\\], return the schema for the table from the user's Planetscale database.",
                "operationId": "planetscaleTableSchemaByName",
                "parameters": [
                    {
                        "name": "databaseName",
                        "in": "query",
                        "description": "Name of the database.",
                        "required": true,
                        "schema": {
                            "type": "string"
                        }
                    },
                    {
                        "name": "tableName",
                        "in": "query",
                        "description": "Name of the table schema to fetch.",
                        "required": true,
                        "schema": {
                            "type": "string"
                        }
                    }
                ],
                "responses": {
                    "200": {
                        "description": "HTTP response containing JSON which describes the table.",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/PlanetscaleAiController.TableSchemaResponse"
                                }
                            }
                        }
                    }
                }
            }
        }
    },
    "components": {
        "schemas": {
            "PlanetscaleAiController.ColumnSchema": {
                "required": [
                    "name",
                    "type"
                ],
                "type": "object",
                "properties": {
                    "name": {
                        "type": "string"
                    },
                    "type": {
                        "$ref": "#/components/schemas/PlanetscaleAiController.ColumnType"
                    }
                },
                "description": "Describes the structure/schema of a single table column."
            },
            "PlanetscaleAiController.ColumnType": {
                "type": "string",
                "description": "Enumeration which lists each type that a column can inhabit in MySQL.",
                "enum": [
                    "STRING",
                    "NUMBER",
                    "BOOLEAN",
                    "DATE",
                    "DATETIME",
                    "TIMESTAMP"
                ]
            },
            "PlanetscaleAiController.ListDatabaseNamesResponse": {
                "required": [
                    "databaseNames"
                ],
                "type": "object",
                "properties": {
                    "databaseNames": {
                        "type": "array",
                        "items": {
                            "type": "string"
                        }
                    }
                },
                "description": "List of database names."
            },
            "PlanetscaleAiController.ListTableNamesResponse": {
                "required": [
                    "databaseName",
                    "tableNames"
                ],
                "type": "object",
                "properties": {
                    "databaseName": {
                        "type": "string"
                    },
                    "tableNames": {
                        "type": "array",
                        "items": {
                            "type": "string"
                        }
                    }
                },
                "description": "List of table names."
            },
            "PlanetscaleAiController.NaturalLanguageQueryResponse": {
                "required": [
                    "query",
                    "results"
                ],
                "type": "object",
                "properties": {
                    "query": {
                        "type": "string"
                    },
                    "results": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "additionalProperties": true
                        }
                    },
                    "columns": {
                        "type": "array",
                        "nullable": true,
                        "items": {
                            "type": "string"
                        }
                    }
                },
                "description": "Response to a natural language SQL query."
            },
            "PlanetscaleAiController.TableSchemaResponse": {
                "required": [
                    "columns",
                    "name"
                ],
                "type": "object",
                "properties": {
                    "name": {
                        "type": "string"
                    },
                    "columns": {
                        "type": "array",
                        "items": {
                            "$ref": "#/components/schemas/PlanetscaleAiController.ColumnSchema"
                        }
                    }
                },
                "description": "Structure which describes a table schema."
            }
        }
    }
};

export default {
    async fetch(request, env) {
        const response = new Response(JSON.stringify(descriptor));
        response.headers.set('Content-Type', 'application/json');
        return response;
    }
}
