import { OpenAPIHono } from "@hono/zod-openapi";
import { swaggerUI } from "@hono/swagger-ui";
import type { Env } from "./types";
import { cacheControl, CacheDuration } from "./cache";
import { getConfig } from "./config";

import packageJson from "../package.json";
import patchesApp from "./routes/patches";
import managerApp from "./routes/manager";
import announcementsApp from "./routes/announcements";
import contributorsApp from "./routes/contributors";
import teamApp from "./routes/team";
import aboutApp from "./routes/about";
import keysApp from "./routes/keys";

type AppBindings = { Bindings: Env };

let _app: OpenAPIHono<AppBindings> | undefined;

export default {
	async fetch(
		request: Request,
		env: Env,
		ctx: ExecutionContext,
	): Promise<Response> {
		if (!_app) {
			const { apiVersion } = getConfig(env);

			_app = new OpenAPIHono<AppBindings>();
			const versionedApp = new OpenAPIHono<AppBindings>();

			// Default 5-minute cache for all versioned routes (overridden per-route where needed)
			versionedApp.use("*", cacheControl(CacheDuration.short));

			versionedApp.route("/patches", patchesApp);
			versionedApp.route("/manager", managerApp);
			versionedApp.route("/announcements", announcementsApp);
			versionedApp.route("/contributors", contributorsApp);
			versionedApp.route("/team", teamApp);
			versionedApp.route("/about", aboutApp);

			_app.route(`/v${apiVersion}`, versionedApp);
			_app.route("/keys", keysApp);

			_app.doc("/openapi", () => ({
				openapi: "3.1.0",
				info: {
					title: "ReVanced API",
					version: packageJson.version,
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

			_app.get("/", swaggerUI({ url: "/openapi" }));
		}
		return _app.fetch(request, env, ctx);
	},
};
