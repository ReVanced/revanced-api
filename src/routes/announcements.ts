import { OpenAPIHono, createRoute, z } from "@hono/zod-openapi";
import type { Env } from "../types";
import { createDb } from "../db/client";
import { announcements } from "../db/schema";
import { eq, desc } from "drizzle-orm";
import { authMiddleware } from "../auth/auth";
import { ErrorResponse } from "../schemas/common";
import {
  AnnouncementResponse,
  AnnouncementsResponse,
  CreateAnnouncementBody,
  UpdateAnnouncementBody,
} from "../schemas/announcements";

const app = new OpenAPIHono<{ Bindings: Env }>();

/* GET /v1/announcements -- returns all announcements as a flat array ordered by id desc, no auth needed, designed for full edge caching (cache: 5min, rate limit: strong 30/2min) */

const listRoute = createRoute({
  method: "get",
  path: "/",
  tags: ["Announcements"],
  summary: "Get all announcements",
  description: "Get all announcements ordered by newest first",
  responses: {
    200: {
      content: { "application/json": { schema: AnnouncementsResponse } },
      description: "All announcements",
    },
  },
});

app.openapi(listRoute, async (c) => {
  const db = createDb(c.env.DB);
  const rows = await db.select().from(announcements).orderBy(desc(announcements.id));

  return c.json(
    rows.map((r) => ({
      id: r.id,
      author: r.author,
      title: r.title,
      content: r.content,
      created_at: r.createdAt,
      archived_at: r.archivedAt,
      level: r.level,
    })),
    200,
  );
});

// POST /v1/announcements (protected, needs bearer token auth)

const createAnnouncementRoute = createRoute({
  method: "post",
  path: "/",
  tags: ["Announcements"],
  summary: "Create an announcement",
  description: "Create a new announcement. Requires bearer token authentication.",
  security: [{ Bearer: [] }],
  request: {
    body: { content: { "application/json": { schema: CreateAnnouncementBody } } },
  },
  responses: {
    201: {
      content: { "application/json": { schema: AnnouncementResponse } },
      description: "The created announcement",
    },
    401: { content: { "application/json": { schema: ErrorResponse } }, description: "Unauthorized" },
    403: { content: { "application/json": { schema: ErrorResponse } }, description: "Forbidden" },
  },
});

app.use("/", async (c, next) => {
  // Only apply auth to non-GET requests
  if (c.req.method !== "GET") {
    return authMiddleware()(c, next);
  }
  await next();
});

app.openapi(createAnnouncementRoute, async (c) => {
  const body = c.req.valid("json");
  const db = createDb(c.env.DB);
  const now = new Date().toISOString().replace(/\.\d{3}Z$/, "");

  const result = await db
    .insert(announcements)
    .values({
      author: body.author ?? null,
      title: body.title,
      content: body.content ?? null,
      createdAt: now,
      level: body.level ?? 0,
    })
    .returning();

  const row = result[0];

  return c.json(
    {
      id: row.id,
      author: row.author,
      title: row.title,
      content: row.content,
      created_at: row.createdAt,
      archived_at: row.archivedAt,
      level: row.level,
    },
    201,
  );
});

/* shared id param schema used by the /:id routes below */

const IdParam = z.object({
  id: z
    .string()
    .regex(/^\d+$/)
    .openapi({ description: "Announcement ID", example: "1", param: { in: "path" } }),
});

// GET /v1/announcements/:id - gets one announcement by id, no auth (cache: 5min, rate limit: weak 5/1min)

const getAnnouncementRoute = createRoute({
  method: "get",
  path: "/{id}",
  tags: ["Announcements"],
  summary: "Get an announcement",
  description: "Get an announcement by its ID",
  request: { params: IdParam },
  responses: {
    200: {
      content: { "application/json": { schema: AnnouncementResponse } },
      description: "The announcement",
    },
    404: {
      content: { "application/json": { schema: ErrorResponse } },
      description: "Announcement not found",
    },
  },
});

app.openapi(getAnnouncementRoute, async (c) => {
  const { id } = c.req.valid("param");
  const db = createDb(c.env.DB);
  const numId = Number(id);

  const rows = await db.select().from(announcements).where(eq(announcements.id, numId));

  if (rows.length === 0) {
    return c.json({ error: "Announcement not found" }, 404);
  }

  const row = rows[0];
  return c.json({
    id: row.id,
    author: row.author,
    title: row.title,
    content: row.content,
    created_at: row.createdAt,
    archived_at: row.archivedAt,
    level: row.level,
  }, 200);
});

/* PATCH /v1/announcements/:id (bearer token required) */

const updateAnnouncementRoute = createRoute({
  method: "patch",
  path: "/{id}",
  tags: ["Announcements"],
  summary: "Update an announcement",
  description: "Update an existing announcement. Requires bearer token authentication.",
  security: [{ Bearer: [] }],
  request: {
    params: IdParam,
    body: { content: { "application/json": { schema: UpdateAnnouncementBody } } },
  },
  responses: {
    200: {
      content: { "application/json": { schema: AnnouncementResponse } },
      description: "The updated announcement",
    },
    401: { content: { "application/json": { schema: ErrorResponse } }, description: "Unauthorized" },
    403: { content: { "application/json": { schema: ErrorResponse } }, description: "Forbidden" },
    404: { content: { "application/json": { schema: ErrorResponse } }, description: "Announcement not found" },
  },
});

app.use("/:id", async (c, next) => {
  if (c.req.method !== "GET") {
    return authMiddleware()(c, next);
  }
  await next();
});

app.openapi(updateAnnouncementRoute, async (c) => {
  const { id } = c.req.valid("param");
  const body = c.req.valid("json");
  const db = createDb(c.env.DB);
  const numId = Number(id);

  // only update fields that were actually sent in the body
  const updates: Record<string, unknown> = {};
  if (body.author !== undefined) updates.author = body.author;
  if (body.title !== undefined) updates.title = body.title;
  if (body.content !== undefined) updates.content = body.content;
  if (body.archived_at !== undefined) updates.archivedAt = body.archived_at;
  if (body.level !== undefined) updates.level = body.level;

  if (Object.keys(updates).length === 0) {
    // Nothing to update — just return the existing row
    const existing = await db.select().from(announcements).where(eq(announcements.id, numId));
    if (existing.length === 0) {
      return c.json({ error: "Announcement not found" }, 404);
    }
    const row = existing[0];
    return c.json({
      id: row.id,
      author: row.author,
      title: row.title,
      content: row.content,
      created_at: row.createdAt,
      archived_at: row.archivedAt,
      level: row.level,
    }, 200);
  }

  const result = await db
    .update(announcements)
    .set(updates)
    .where(eq(announcements.id, numId))
    .returning();

  if (result.length === 0) {
    return c.json({ error: "Announcement not found" }, 404);
  }

  const row = result[0];
  return c.json({
    id: row.id,
    author: row.author,
    title: row.title,
    content: row.content,
    created_at: row.createdAt,
    archived_at: row.archivedAt,
    level: row.level,
  }, 200);
});

// DELETE /v1/announcements/:id -- also needs auth

const deleteAnnouncementRoute = createRoute({
  method: "delete",
  path: "/{id}",
  tags: ["Announcements"],
  summary: "Delete an announcement",
  description: "Delete an announcement. Requires bearer token authentication.",
  security: [{ Bearer: [] }],
  request: { params: IdParam },
  responses: {
    204: { description: "Announcement deleted" },
    401: { content: { "application/json": { schema: ErrorResponse } }, description: "Unauthorized" },
    403: { content: { "application/json": { schema: ErrorResponse } }, description: "Forbidden" },
    404: { content: { "application/json": { schema: ErrorResponse } }, description: "Announcement not found" },
  },
});

app.openapi(deleteAnnouncementRoute, async (c) => {
  const { id } = c.req.valid("param");
  const db = createDb(c.env.DB);
  const numId = Number(id);

  const result = await db.delete(announcements).where(eq(announcements.id, numId)).returning();

  if (result.length === 0) {
    return c.json({ error: "Announcement not found" }, 404);
  }

  return c.body(null, 204);
});

export default app;
