import { OpenAPIHono, createRoute } from "@hono/zod-openapi";
import type { Env } from "../types";
import { PrereleaseQuerySchema, ErrorResponseSchema } from "../schemas/common";
import {
  ReleaseResponseSchema,
  VersionResponseSchema,
  HistoryResponseSchema,
  PublicKeyResponseSchema,
} from "../schemas/releases";
import * as patchesService from "../services/patches";
import { cacheControl, CacheDuration } from "../cache";

const app = new OpenAPIHono<{ Bindings: Env }>();

app.openapi(
  createRoute({
    method: "get",
    path: "/",
    tags: ["Patches"],
    summary: "Get current patches release",
    description: "Get the current patches release.",
    request: { query: PrereleaseQuerySchema },
    responses: {
      200: {
        content: { "application/json": { schema: ReleaseResponseSchema } },
        description: "The current patches release.",
      },
      500: {
        content: { "application/json": { schema: ErrorResponseSchema } },
        description: "GitHub API error.",
      },
    },
  }),
  async (c) => {
    const { prerelease } = c.req.valid("query");
    try {
      return c.json(await patchesService.getRelease(c.env, prerelease === "true"), 200);
    } catch (error) {
      return c.json({ error: error instanceof Error ? error.message : "Unknown error" }, 500);
    }
  },
);

app.openapi(
  createRoute({
    method: "get",
    path: "/version",
    tags: ["Patches"],
    summary: "Get current patches release version",
    description: "Get the current patches release version.",
    request: { query: PrereleaseQuerySchema },
    responses: {
      200: {
        content: { "application/json": { schema: VersionResponseSchema } },
        description: "The current patches release version.",
      },
      500: {
        content: { "application/json": { schema: ErrorResponseSchema } },
        description: "GitHub API error.",
      },
    },
  }),
  async (c) => {
    const { prerelease } = c.req.valid("query");
    try {
      return c.json(await patchesService.getVersion(c.env, prerelease === "true"), 200);
    } catch (error) {
      return c.json({ error: error instanceof Error ? error.message : "Unknown error" }, 500);
    }
  },
);

app.openapi(
  createRoute({
    method: "get",
    path: "/history",
    tags: ["Patches"],
    summary: "Get patches release history",
    description: "Get the patches release history (changelog).",
    request: { query: PrereleaseQuerySchema },
    responses: {
      200: {
        content: { "application/json": { schema: HistoryResponseSchema } },
        description: "The patches release history.",
      },
      404: { description: "No patches release history configured." },
      500: {
        content: { "application/json": { schema: ErrorResponseSchema } },
        description: "GitHub API error.",
      },
    },
  }),
  async (c) => {
    const { prerelease } = c.req.valid("query");
    try {
      const result = await patchesService.getHistory(c.env, prerelease === "true");
      if (!result) {
        return c.body(null, 404);
      }
      return c.json(result, 200);
    } catch (error) {
      return c.json({ error: error instanceof Error ? error.message : "Unknown error" }, 500);
    }
  },
);

app.openapi(
  createRoute({
    method: "get",
    path: "/keys",
    tags: ["Patches"],
    summary: "Get patches public keys",
    description: "Get the public keys for verifying patches assets.",
    responses: {
      200: {
        content: { "application/json": { schema: PublicKeyResponseSchema } },
        description: "The public keys.",
      },
    },
  }),
  async (c) => {
    return c.json(await patchesService.getPublicKey(c.env), 200);
  },
);

export default app;
