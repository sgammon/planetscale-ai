const descriptor = {
    "schema_version": "v1",
    "name_for_human": "Planetscale DB Agent",
    "name_for_model": "planetscale",
    "description_for_human": "Plugin for connecting to a Planetscale database and performing queries using natural language.",
    "description_for_model": "Plugin for connecting to a database and performing queries using natural language inputs from the user; the service translates the queries to SQL, then executes the queries, then provides the results back to the AI.",
    "auth": {
        "type": "none"
    },
    "api": {
        "type": "openapi",
        "url": "https://elide.dev/planetscale-ai/openapi.json",
        "is_user_authenticated": false
    },
    "logo_url": "https://dbagent.io/static/logo.png",
    "contact_email": "support@dbagent.io",
    "legal_info_url": "https://dbagent.io/legal"
};

export default {
    async fetch(request, env) {
        const response = new Response(JSON.stringify(descriptor));
        response.headers.set('Content-Type', 'application/json');
        return response;
    }
}
