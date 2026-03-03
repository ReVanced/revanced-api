import { OpenAPIHono, createRoute } from "@hono/zod-openapi";
import type { Env, AppVariables } from "../types";
import { ErrorResponse } from "../schemas/common";
import { ContributorsResponse } from "../schemas/contributors";

const app = new OpenAPIHono<{ Bindings: Env; Variables: AppVariables }>();

// GET /contributors
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

let _contributorRepos: { repo: string; name: string }[] | undefined;

app.openapi(getContributorsRoute, async (c) => {
  const backend = c.get("backend");
  _contributorRepos ??= c.env.CONTRIBUTORS_REPOS.split(",").map((entry) => {
    const [repo, ...nameParts] = entry.trim().split(":");
    return { repo: repo.trim(), name: nameParts.join(":").trim() };
  });
  const contributorRepos = _contributorRepos;
  const org = c.env.ORGANIZATION;

  try {
    const results = await Promise.all(
      contributorRepos.map(async ({ repo, name }) => {
        const contributors = await backend.contributors(org, repo);
        return {
          name,
          url: `https://github.com/${org}/${repo}`,
          contributors: contributors.map((contributor) => ({
            name: contributor.name,
            avatar_url: contributor.avatarUrl,
            url: contributor.url,
            contributions: contributor.contributions,
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
