## planetscale ai

it's a plugin for openAI that allows you to access your PlanetScale database. you can ask it stuff about your DB, or ask
it for optimization opportunities. we are working on making it dispatchable from github as well.

### how to use it

select the planetscale plugin in the openAI interface, and then write and submit a prompt. it will dispatch this bot,
which then dispatches OpenAI to generate a query from your natural language input. that query is then submitted to your
planetscale db, the results are interpreted, and returned to your convo.

### is it done yet

yes
