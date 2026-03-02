import { OpenAPIHono, createRoute } from "@hono/zod-openapi";
import type { Env, AppVariables } from "../types";
import { PrereleaseQuery, ErrorResponse } from "../schemas/common";
import { ReleaseResponse, VersionResponse, HistoryResponse, PublicKeyResponse } from "../schemas/releases";

const app = new OpenAPIHono<{ Bindings: Env; Variables: AppVariables }>();

// GET /patches
const getPatchesRoute = createRoute({
  method: "get",
  path: "/",
  tags: ["Patches"],
  summary: "Get current patches release",
  description: "Get the current patches release",
  request: { query: PrereleaseQuery },
  responses: {
    200: { content: { "application/json": { schema: ReleaseResponse } }, description: "The current patches release" },
    500: { content: { "application/json": { schema: ErrorResponse } }, description: "GitHub API error" },
  },
});

app.openapi(getPatchesRoute, async (c) => {
  const { prerelease } = c.req.valid("query");
  const backend = c.get("backend");
  const { patchesAssetRegex, patchesSignatureAssetRegex } = c.get("config");

  try {
    const release = await backend.release(c.env.ORGANIZATION, c.env.PATCHES_REPO, prerelease === "true");
    const asset = release.assets.find((a) => patchesAssetRegex.test(a.name));
    const sigAsset = release.assets.find((a) => patchesSignatureAssetRegex.test(a.name));

    return c.json({
      version: release.tag,
      created_at: release.createdAt,
      description: release.releaseNote,
      download_url: asset?.downloadUrl ?? "",
      signature_download_url: sigAsset?.downloadUrl ?? null,
    }, 200);
  } catch (e) {
    return c.json({ error: e instanceof Error ? e.message : "Unknown error" }, 500);
  }
});

// GET /patches/version
const getPatchesVersionRoute = createRoute({
  method: "get",
  path: "/version",
  tags: ["Patches"],
  summary: "Get current patches release version",
  description: "Get the current patches release version",
  request: { query: PrereleaseQuery },
  responses: {
    200: { content: { "application/json": { schema: VersionResponse } }, description: "The current patches release version" },
    500: { content: { "application/json": { schema: ErrorResponse } }, description: "GitHub API error" },
  },
});

app.openapi(getPatchesVersionRoute, async (c) => {
  const { prerelease } = c.req.valid("query");
  const backend = c.get("backend");

  try {
    const release = await backend.release(c.env.ORGANIZATION, c.env.PATCHES_REPO, prerelease === "true");
    return c.json({ version: release.tag }, 200);
  } catch (e) {
    return c.json({ error: e instanceof Error ? e.message : "Unknown error" }, 500);
  }
});

// GET /patches/history
const getPatchesHistoryRoute = createRoute({
  method: "get",
  path: "/history",
  tags: ["Patches"],
  summary: "Get patches release history",
  description: "Get the patches release history (changelog)",
  request: { query: PrereleaseQuery },
  responses: {
    200: { content: { "application/json": { schema: HistoryResponse } }, description: "The patches release history" },
    404: { description: "No patches release history configured" },
    500: { content: { "application/json": { schema: ErrorResponse } }, description: "GitHub API error" },
  },
});

app.openapi(getPatchesHistoryRoute, async (c) => {
  const { prerelease } = c.req.valid("query");
  const historyFile = c.env.PATCHES_HISTORY_FILE;

  if (!historyFile) {
    return c.body(null, 404);
  }

  const backend = c.get("backend");
  const branch = prerelease === "true" ? c.env.PRERELEASE_BRANCH : c.env.MAIN_BRANCH;

  try {
    const content = await backend.fileContent(c.env.ORGANIZATION, c.env.PATCHES_REPO, branch, historyFile);
    return c.json({ history: content }, 200);
  } catch (e) {
    return c.json({ error: e instanceof Error ? e.message : "Unknown error" }, 500);
  }
});

// GET /patches/keys
const getPatchesKeysRoute = createRoute({
  method: "get",
  path: "/keys",
  tags: ["Patches"],
  summary: "Get patches public keys",
  description: "Get the public keys for verifying patches assets",
  responses: {
    200: {
      content: { "application/json": { schema: PublicKeyResponse } },
      description: "The public keys",
    },
  },
});

app.openapi(getPatchesKeysRoute, (c) => {
  return c.json({
    patches_public_key: c.env.PATCHES_PUBLIC_KEY,
  }, 200);
});

export default app;
