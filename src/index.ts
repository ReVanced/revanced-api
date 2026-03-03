import { OpenAPIHono } from "@hono/zod-openapi";
import { swaggerUI } from "@hono/swagger-ui";
import type { Env, AppVariables, Database } from "./types";
import { GitHubBackend } from "./backend/github";
import { createDb } from "./db/client";

import pkg from "../package.json";
import patchesApp from "./routes/patches";
import managerApp from "./routes/manager";
import announcementsApp from "./routes/announcements";
import contributorsApp from "./routes/contributors";
import teamApp from "./routes/team";
import aboutApp from "./routes/about";

const VERSION = pkg.version;

type AppBindings = { Bindings: Env; Variables: AppVariables };

const app = new OpenAPIHono<AppBindings>();

// v1 sub-app — all routes are mounted here under a single prefix
const v1 = new OpenAPIHono<AppBindings>();

// Middleware: share GitHubBackend and db across all routes (lazily cached)
let _backend: GitHubBackend | undefined;
let _db: Database | undefined;

v1.use("*", async (c, next) => {
  _backend ??= new GitHubBackend(c.env.GITHUB_TOKEN);
  _db ??= createDb(c.env.DB);

  c.set("backend", _backend);
  c.set("db", _db);

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
