import { OpenAPIHono } from "@hono/zod-openapi";
import { swaggerUI } from "@hono/swagger-ui";
import type { Env } from "./types";

import patchesApp from "./routes/patches";
import managerApp from "./routes/manager";
import announcementsApp from "./routes/announcements";
import contributorsApp from "./routes/contributors";
import teamApp from "./routes/team";
import aboutApp from "./routes/about";
import rateLimitApp from "./routes/rate-limit";
import pingApp from "./routes/ping";

/* version string, bundled at build time (from package.json i think) */
const VERSION = "1.0.0";

const app = new OpenAPIHono<{ Bindings: Env }>();

// mounting all the route groups under /v1

app.route("/v1/patches", patchesApp);
app.route("/v1/manager", managerApp);
app.route("/v1/announcements", announcementsApp);
app.route("/v1/contributors", contributorsApp);
app.route("/v1/team", teamApp);
app.route("/v1/about", aboutApp);
app.route("/v1/backend/rate_limit", rateLimitApp);
app.route("/v1/ping", pingApp);

/* openapi spec endpoint -- serves the json spec at /openapi */

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

// swagger ui served at root "/" so you can see the api docs

app.get("/", swaggerUI({ url: "/openapi" }));

/* default export for cloudflare workers - for node.js do: import { serve } from '@hono/node-server' then serve(app), for vercel: import { handle } from 'hono/vercel' then export const GET = handle(app) and POST too */
export default app;
