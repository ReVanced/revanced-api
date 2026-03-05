import { OpenAPIHono, createRoute } from "@hono/zod-openapi";
import type { Env } from "../types";
import { PrereleaseQuerySchema, ErrorResponseSchema } from "../schemas/common";
import { ReleaseResponseSchema, VersionResponseSchema, HistoryResponseSchema } from "../schemas/releases";
import * as managerService from "../services/manager";
import { cacheControl, CacheDuration } from "../cache";

const app = new OpenAPIHono<{ Bindings: Env }>();

app.openapi(
  createRoute({
    method: "get",
    path: "/",
    tags: ["Manager"],
    summary: "Get current manager release",
    description: "Get the current manager release.",
    request: { query: PrereleaseQuerySchema },
    responses: {
      200: {
        content: { "application/json": { schema: ReleaseResponseSchema } },
        description: "The latest manager release.",
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
      return c.json(await managerService.getRelease(c.env, prerelease === "true"), 200);
    } catch (error) {
      return c.json({ error: error instanceof Error ? error.message : "Unknown error" }, 500);
    }
  },
);

app.openapi(
  createRoute({
    method: "get",
    path: "/version",
    tags: ["Manager"],
    summary: "Get current manager release version",
    description: "Get the current manager release version.",
    request: { query: PrereleaseQuerySchema },
    responses: {
      200: {
        content: { "application/json": { schema: VersionResponseSchema } },
        description: "The current manager release version.",
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
      return c.json(await managerService.getVersion(c.env, prerelease === "true"), 200);
    } catch (error) {
      return c.json({ error: error instanceof Error ? error.message : "Unknown error" }, 500);
    }
  },
);

app.openapi(
  createRoute({
    method: "get",
    path: "/history",
    tags: ["Manager"],
    summary: "Get manager release history",
    description: "Get the manager release history (changelog).",
    request: { query: PrereleaseQuerySchema },
    responses: {
      200: {
        content: { "application/json": { schema: HistoryResponseSchema } },
        description: "The manager release history.",
      },
      404: { description: "No manager release history configured." },
      500: {
        content: { "application/json": { schema: ErrorResponseSchema } },
        description: "GitHub API error.",
      },
    },
  }),
  async (c) => {
    const { prerelease } = c.req.valid("query");
    try {
      const result = await managerService.getHistory(c.env, prerelease === "true");
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
    path: "/downloaders",
    tags: ["Manager"],
    summary: "Get current manager downloaders release",
    description: "Get the current manager downloaders release.",
    request: { query: PrereleaseQuerySchema },
    responses: {
      200: {
        content: { "application/json": { schema: ReleaseResponseSchema } },
        description: "The latest manager downloaders release.",
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
      return c.json(await managerService.getDownloadersRelease(c.env, prerelease === "true"), 200);
    } catch (error) {
      return c.json({ error: error instanceof Error ? error.message : "Unknown error" }, 500);
    }
  },
);

app.openapi(
  createRoute({
    method: "get",
    path: "/downloaders/version",
    tags: ["Manager"],
    summary: "Get current manager downloaders release version",
    description: "Get the current manager downloaders release version.",
    request: { query: PrereleaseQuerySchema },
    responses: {
      200: {
        content: { "application/json": { schema: VersionResponseSchema } },
        description: "The current manager downloaders release version.",
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
      return c.json(await managerService.getDownloadersVersion(c.env, prerelease === "true"), 200);
    } catch (error) {
      return c.json({ error: error instanceof Error ? error.message : "Unknown error" }, 500);
    }
  },
);

export default app;
