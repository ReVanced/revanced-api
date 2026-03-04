import { OpenAPIHono } from "@hono/zod-openapi";
import { swaggerUI } from "@hono/swagger-ui";
import type { Env } from "./types";

import packageJson from "../package.json";
import patchesApp from "./routes/patches";
import managerApp from "./routes/manager";
import announcementsApp from "./routes/announcements";
import contributorsApp from "./routes/contributors";
import teamApp from "./routes/team";
import aboutApp from "./routes/about";
import keysApp from "./routes/keys";

const VERSION = packageJson.version;

type AppBindings = { Bindings: Env };

const app = new OpenAPIHono<AppBindings>();
const v1App = new OpenAPIHono<AppBindings>();

v1App.route("/patches", patchesApp);
v1App.route("/manager", managerApp);
v1App.route("/announcements", announcementsApp);
v1App.route("/contributors", contributorsApp);
v1App.route("/team", teamApp);
v1App.route("/about", aboutApp);

app.route("/v1", v1App);
app.route("/keys", keysApp);

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

app.get("/", swaggerUI({ url: "/openapi" }));

export default app;
