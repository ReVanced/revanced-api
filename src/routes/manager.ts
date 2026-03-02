import { OpenAPIHono, createRoute } from "@hono/zod-openapi";
import type { Env, AppVariables } from "../types";
import { PrereleaseQuery, ErrorResponse } from "../schemas/common";
import { ReleaseResponse, VersionResponse, HistoryResponse } from "../schemas/releases";

const app = new OpenAPIHono<{ Bindings: Env; Variables: AppVariables }>();

// GET /manager
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
  const backend = c.get("backend");
  const { managerAssetRegex } = c.get("config");

  try {
    const release = await backend.release(c.env.ORGANIZATION, c.env.MANAGER_REPO, prerelease === "true");
    const asset = release.assets.find((a) => managerAssetRegex.test(a.name));

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

// GET /manager/version
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
  const backend = c.get("backend");

  try {
    const release = await backend.release(c.env.ORGANIZATION, c.env.MANAGER_REPO, prerelease === "true");
    return c.json({ version: release.tag }, 200);
  } catch (e) {
    return c.json({ error: e instanceof Error ? e.message : "Unknown error" }, 500);
  }
});

// GET /manager/history
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

  const backend = c.get("backend");
  const branch = prerelease === "true" ? c.env.PRERELEASE_BRANCH : c.env.MAIN_BRANCH;

  try {
    const content = await backend.fileContent(c.env.ORGANIZATION, c.env.MANAGER_REPO, branch, historyFile);
    return c.json({ history: content }, 200);
  } catch (e) {
    return c.json({ error: e instanceof Error ? e.message : "Unknown error" }, 500);
  }
});

// GET /manager/downloaders
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
  const backend = c.get("backend");
  const { managerDownloadersAssetRegex } = c.get("config");

  try {
    const release = await backend.release(
      c.env.ORGANIZATION,
      c.env.MANAGER_DOWNLOADERS_REPO,
      prerelease === "true",
    );
    const asset = release.assets.find((a) => managerDownloadersAssetRegex.test(a.name));

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

// GET /manager/downloaders/version
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
  const backend = c.get("backend");

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
