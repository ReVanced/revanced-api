import { OpenAPIHono, createRoute } from "@hono/zod-openapi";
import type { Env } from "../types";
import { GitHubBackend } from "../backend/github";
import { PrereleaseQuery, ErrorResponse } from "../schemas/common";
import { ReleaseResponse, VersionResponse, HistoryResponse } from "../schemas/releases";

const app = new OpenAPIHono<{ Bindings: Env }>();

/* GET /v1/manager - cache 5min, rate limit weak (5/1min) */

const getManagerRoute = createRoute({
  method: "get",
  path: "/",
  tags: ["Manager"],
  summary: "Get current manager release",
  description: "Get the current manager release",
  request: { query: PrereleaseQuery },
  responses: {
    200: { content: { "application/json": { schema: ReleaseResponse } }, description: "The latest manager release" },
    500: { content: { "application/json": { schema: ErrorResponse } }, description: "GitHub API error" },
  },
});

app.openapi(getManagerRoute, async (c) => {
  const { prerelease } = c.req.valid("query");
  const backend = new GitHubBackend(c.env.GITHUB_TOKEN);
  const assetRegex = new RegExp(c.env.MANAGER_ASSET_REGEX);

  try {
    const release = await backend.release(c.env.ORGANIZATION, c.env.MANAGER_REPO, prerelease === "true");
    const asset = release.assets.find((a) => assetRegex.test(a.name));

    return c.json({
      version: release.tag,
      created_at: release.createdAt,
      description: release.releaseNote,
      download_url: asset?.downloadUrl ?? "",
    }, 200);
  } catch (e) {
    return c.json({ error: e instanceof Error ? e.message : "Unknown error" }, 500);
  }
});

// GET /v1/manager/version -- cache 5min, rate limit weak

const getManagerVersionRoute = createRoute({
  method: "get",
  path: "/version",
  tags: ["Manager"],
  summary: "Get current manager release version",
  description: "Get the current manager release version",
  request: { query: PrereleaseQuery },
  responses: {
    200: { content: { "application/json": { schema: VersionResponse } }, description: "The current manager release version" },
    500: { content: { "application/json": { schema: ErrorResponse } }, description: "GitHub API error" },
  },
});

app.openapi(getManagerVersionRoute, async (c) => {
  const { prerelease } = c.req.valid("query");
  const backend = new GitHubBackend(c.env.GITHUB_TOKEN);

  try {
    const release = await backend.release(c.env.ORGANIZATION, c.env.MANAGER_REPO, prerelease === "true");
    return c.json({ version: release.tag }, 200);
  } catch (e) {
    return c.json({ error: e instanceof Error ? e.message : "Unknown error" }, 500);
  }
});

/* GET /v1/manager/history (cache 5min) */ // rate limit weak 5/1min

const getManagerHistoryRoute = createRoute({
  method: "get",
  path: "/history",
  tags: ["Manager"],
  summary: "Get manager release history",
  description: "Get the manager release history (changelog)",
  request: { query: PrereleaseQuery },
  responses: {
    200: { content: { "application/json": { schema: HistoryResponse } }, description: "The manager release history" },
    404: { description: "No manager release history configured" },
    500: { content: { "application/json": { schema: ErrorResponse } }, description: "GitHub API error" },
  },
});

app.openapi(getManagerHistoryRoute, async (c) => {
  const { prerelease } = c.req.valid("query");
  const historyFile = c.env.MANAGER_HISTORY_FILE;

  if (!historyFile) {
    return c.body(null, 404);
  }

  const backend = new GitHubBackend(c.env.GITHUB_TOKEN);
  const branch = prerelease === "true" ? c.env.PRERELEASE_BRANCH : c.env.MAIN_BRANCH;

  try {
    const content = await backend.fileContent(c.env.ORGANIZATION, c.env.MANAGER_REPO, branch, historyFile);
    return c.json({ history: content }, 200);
  } catch (e) {
    return c.json({ error: e instanceof Error ? e.message : "Unknown error" }, 500);
  }
});

// GET /v1/manager/downloaders (cache: 5min, rate limit: weak)

const getDownloadersRoute = createRoute({
  method: "get",
  path: "/downloaders",
  tags: ["Manager"],
  summary: "Get current manager downloaders release",
  description: "Get the current manager downloaders release",
  request: { query: PrereleaseQuery },
  responses: {
    200: { content: { "application/json": { schema: ReleaseResponse } }, description: "The latest manager downloaders release" },
    500: { content: { "application/json": { schema: ErrorResponse } }, description: "GitHub API error" },
  },
});

app.openapi(getDownloadersRoute, async (c) => {
  const { prerelease } = c.req.valid("query");
  const backend = new GitHubBackend(c.env.GITHUB_TOKEN);
  const assetRegex = new RegExp(c.env.MANAGER_DOWNLOADERS_ASSET_REGEX);

  try {
    const release = await backend.release(
      c.env.ORGANIZATION,
      c.env.MANAGER_DOWNLOADERS_REPO,
      prerelease === "true",
    );
    const asset = release.assets.find((a) => assetRegex.test(a.name));

    return c.json({
      version: release.tag,
      created_at: release.createdAt,
      description: release.releaseNote,
      download_url: asset?.downloadUrl ?? "",
    }, 200);
  } catch (e) {
    return c.json({ error: e instanceof Error ? e.message : "Unknown error" }, 500);
  }
});

/* GET /v1/manager/downloaders/version -- same cache and rate limit as the other manager ones */

const getDownloadersVersionRoute = createRoute({
  method: "get",
  path: "/downloaders/version",
  tags: ["Manager"],
  summary: "Get current manager downloaders release version",
  description: "Get the current manager downloaders release version",
  request: { query: PrereleaseQuery },
  responses: {
    200: { content: { "application/json": { schema: VersionResponse } }, description: "The current manager downloaders release version" },
    500: { content: { "application/json": { schema: ErrorResponse } }, description: "GitHub API error" },
  },
});

app.openapi(getDownloadersVersionRoute, async (c) => {
  const { prerelease } = c.req.valid("query");
  const backend = new GitHubBackend(c.env.GITHUB_TOKEN);

  try {
    const release = await backend.release(
      c.env.ORGANIZATION,
      c.env.MANAGER_DOWNLOADERS_REPO,
      prerelease === "true",
    );
    return c.json({ version: release.tag }, 200);
  } catch (e) {
    return c.json({ error: e instanceof Error ? e.message : "Unknown error" }, 500);
  }
});

export default app;
