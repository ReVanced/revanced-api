import { OpenAPIHono, createRoute } from "@hono/zod-openapi";
import type { Env } from "../types";
import { GitHubBackend } from "../backend/github";
import { ErrorResponse } from "../schemas/common";
import { ContributorsResponse } from "../schemas/contributors";

const app = new OpenAPIHono<{ Bindings: Env }>();

/* GET /v1/contributors (cache: 1 day, rate limit: strong 30/2min) */

const getContributorsRoute = createRoute({
  method: "get",
  path: "/",
  tags: ["API"],
  summary: "Get contributors",
  description: "Get the list of contributors for each configured repository",
  responses: {
    200: {
      content: { "application/json": { schema: ContributorsResponse } },
      description: "The list of contributors",
    },
    500: {
      content: { "application/json": { schema: ErrorResponse } },
      description: "GitHub API error",
    },
  },
});

app.openapi(getContributorsRoute, async (c) => {
  const backend = new GitHubBackend(c.env.GITHUB_TOKEN);
  const org = c.env.ORGANIZATION;

  /* parse the "repo:Friendly Name,repo2:Friendly Name 2" format from the env var */
  const repoEntries = c.env.CONTRIBUTORS_REPOS.split(",").map((entry) => {
    const [repo, ...nameParts] = entry.trim().split(":");
    return { repo: repo.trim(), name: nameParts.join(":").trim() };
  });

  try {
    const results = await Promise.all(
      repoEntries.map(async ({ repo, name }) => {
        const contributors = await backend.contributors(org, repo);
        return {
          name,
          url: `https://github.com/${org}/${repo}`,
          contributors: contributors.map((c) => ({
            name: c.name,
            avatar_url: c.avatarUrl,
            url: c.url,
            contributions: c.contributions,
          })),
        };
      }),
    );

    return c.json(results, 200);
  } catch (e) {
    return c.json({ error: e instanceof Error ? e.message : "Unknown error" }, 500);
  }
});

export default app;
