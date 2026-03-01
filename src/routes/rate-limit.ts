import { OpenAPIHono, createRoute } from "@hono/zod-openapi";
import type { Env } from "../types";
import { GitHubBackend } from "../backend/github";
import { ErrorResponse } from "../schemas/common";
import { RateLimitResponse } from "../schemas/releases";

const app = new OpenAPIHono<{ Bindings: Env }>();

// GET /v1/backend/rate_limit (no cache on this one | rate limit: weak 5/1min)

const getRateLimitRoute = createRoute({
  method: "get",
  path: "/",
  tags: ["API"],
  summary: "Get rate limit of backend",
  description: "Get the rate limit of the backend (GitHub API)",
  responses: {
    200: {
      content: { "application/json": { schema: RateLimitResponse } },
      description: "The rate limit of the backend",
    },
    404: { description: "Rate limit information not available" },
    500: {
      content: { "application/json": { schema: ErrorResponse } },
      description: "GitHub API error",
    },
  },
});

app.openapi(getRateLimitRoute, async (c) => {
  const backend = new GitHubBackend(c.env.GITHUB_TOKEN);

  try {
    const rateLimit = await backend.rateLimit();

    if (!rateLimit) {
      return c.body(null, 404);
    }

    return c.json(
      {
        limit: rateLimit.limit,
        remaining: rateLimit.remaining,
        reset: rateLimit.reset,
      },
      200,
    );
  } catch (e) {
    return c.json({ error: e instanceof Error ? e.message : "Unknown error" }, 500);
  }
});

export default app;
