const descriptor = {
    "schema_version": "v1",
    "name_for_human": "PlanetScale",
    "name_for_model": "planetscale",
    "description_for_human": "Plugin for connecting to a Planetscale database and performing queries using natural language.",
    "description_for_model": "Plugin for connecting to a database and performing queries using natural language inputs from the user; the service translates the queries to SQL, then executes the queries, then provides the results back to the AI.",
    "auth": {
        "type": "none"
    },
    "api": {
        "type": "openapi",
        "url": "https://planetscale.ai/openapi.json",
        "is_user_authenticated": false
    },
    "logo_url": "https://assets.planetscale-ai-api.elide.dev/assets/planetscale-logo.jpg",
    "contact_email": "support@elide.cloud",
    "legal_info_url": "https://less.build/legal"
};

export default {
    async fetch(request, env) {
        const response = new Response(JSON.stringify(descriptor));
        response.headers.set('Content-Type', 'application/json');
        return response;
    }
}
