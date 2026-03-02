import { OpenAPIHono } from "@hono/zod-openapi";
import { swaggerUI } from "@hono/swagger-ui";
import type { Env, AppVariables } from "./types";
import { GitHubBackend } from "./backend/github";
import { createDb } from "./db/client";

import patchesApp from "./routes/patches";
import managerApp from "./routes/manager";
import announcementsApp from "./routes/announcements";
import contributorsApp from "./routes/contributors";
import teamApp from "./routes/team";
import aboutApp from "./routes/about";

// Version string
const VERSION = "1.0.0";

type AppBindings = { Bindings: Env; Variables: AppVariables };

const app = new OpenAPIHono<AppBindings>();

// v1 sub-app — all routes are mounted here under a single prefix
const v1 = new OpenAPIHono<AppBindings>();

// Middleware: share GitHubBackend, db, and parsed config across all routes
v1.use("*", async (c, next) => {
  const backend = new GitHubBackend(c.env.GITHUB_TOKEN);
  const db = createDb(c.env.DB);

  const config = {
    patchesAssetRegex: new RegExp(c.env.PATCHES_ASSET_REGEX),
    patchesSignatureAssetRegex: new RegExp(c.env.PATCHES_SIGNATURE_ASSET_REGEX),
    managerAssetRegex: new RegExp(c.env.MANAGER_ASSET_REGEX),
    managerDownloadersAssetRegex: new RegExp(c.env.MANAGER_DOWNLOADERS_ASSET_REGEX),
    contributorRepos: c.env.CONTRIBUTORS_REPOS.split(",").map((entry) => {
      const [repo, ...nameParts] = entry.trim().split(":");
      return { repo: repo.trim(), name: nameParts.join(":").trim() };
    }),
  };

  c.set("backend", backend);
  c.set("db", db);
  c.set("config", config);

  await next();
});

v1.route("/patches", patchesApp);
v1.route("/manager", managerApp);
v1.route("/announcements", announcementsApp);
v1.route("/contributors", contributorsApp);
v1.route("/team", teamApp);
v1.route("/about", aboutApp);

app.route("/v1", v1);

// OpenAPI spec endpoint
app.doc("/openapi", () => ({
  openapi: "3.1.0",
  info: {
    title: "ReVanced API",
    version: VERSION,
    description: "API server for ReVanced.",
    contact: {
      name: "ReVanced",
      url: "https://revanced.app",
      email: "contact@revanced.app",
    },
    license: {
      name: "AGPLv3",
      url: "https://github.com/ReVanced/revanced-api/blob/main/LICENSE",
    },
  },
  security: [],
  servers: [
    { url: "https://api.revanced.app", description: "Production" },
  ],
}));

// Swagger UI served at root
app.get("/", swaggerUI({ url: "/openapi" }));

export default app;
