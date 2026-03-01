import { OpenAPIHono, createRoute } from "@hono/zod-openapi";
import type { Env } from "../types";

const app = new OpenAPIHono<{ Bindings: Env }>();

/* GET /v1/ping - no cache, no rate limit, just checks if the server is alive */

const pingRoute = createRoute({
  method: "get",
  path: "/",
  tags: ["API"],
  summary: "Ping",
  description: "Ping the server to check if it's reachable",
  responses: {
    204: { description: "The server is reachable" },
  },
});

app.openapi(pingRoute, (c) => {
  return c.body(null, 204);
});

export default app;
