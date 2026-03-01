import { OpenAPIHono, createRoute } from "@hono/zod-openapi";
import type { Env } from "../types";
import { GitHubBackend } from "../backend/github";
import { ErrorResponse } from "../schemas/common";
import { TeamResponse } from "../schemas/contributors";

const app = new OpenAPIHono<{ Bindings: Env }>();

// GET /v1/team -- cache 1 day, rate limit strong (30/2min)

const getTeamRoute = createRoute({
  method: "get",
  path: "/",
  tags: ["API"],
  summary: "Get team members",
  description: "Get the list of team members from the organization",
  responses: {
    200: {
      content: { "application/json": { schema: TeamResponse } },
      description: "The list of team members",
    },
    500: {
      content: { "application/json": { schema: ErrorResponse } },
      description: "GitHub API error",
    },
  },
});

app.openapi(getTeamRoute, async (c) => {
  const backend = new GitHubBackend(c.env.GITHUB_TOKEN);

  try {
    const members = await backend.members(c.env.ORGANIZATION);

    return c.json(
      members.map((m) => ({
        name: m.name,
        avatar_url: m.avatarUrl,
        url: m.url,
        bio: m.bio,
        gpg_key:
          m.gpgKeys.ids.length > 0
            ? { id: m.gpgKeys.ids[0], url: m.gpgKeys.url }
            : null,
      })),
      200,
    );
  } catch (e) {
    return c.json({ error: e instanceof Error ? e.message : "Unknown error" }, 500);
  }
});

export default app;
