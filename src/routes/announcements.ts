import { OpenAPIHono, createRoute } from "@hono/zod-openapi";
import type { Env, AppVariables } from "../types";
import { announcements } from "../db/schema";
import { eq, desc } from "drizzle-orm";
import { authMiddleware } from "../auth/auth";
import { ErrorResponse, AnnouncementIdParam } from "../schemas/common";
import {
  AnnouncementResponse,
  AnnouncementsResponse,
  CreateAnnouncementBody,
  UpdateAnnouncementBody,
} from "../schemas/announcements";

const app = new OpenAPIHono<{ Bindings: Env; Variables: AppVariables }>();

// GET /announcements — list all announcements ordered by newest first
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
  const db = c.get("db");
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

// POST /announcements — create an announcement (protected)
const createAnnouncementRoute = createRoute({
  method: "post",
  path: "/",
  tags: ["Announcements"],
  summary: "Create an announcement",
  description: "Create a new announcement. Requires bearer token authentication.",
  security: [{ Bearer: [] }],
  middleware: [authMiddleware()],
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

app.openapi(createAnnouncementRoute, async (c) => {
  const body = c.req.valid("json");
  const db = c.get("db");
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

// GET /announcements/:id — get a single announcement by id
const getAnnouncementRoute = createRoute({
  method: "get",
  path: "/{id}",
  tags: ["Announcements"],
  summary: "Get an announcement",
  description: "Get an announcement by its ID",
  request: { params: AnnouncementIdParam },
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
  const db = c.get("db");

  const rows = await db.select().from(announcements).where(eq(announcements.id, id));

  if (rows.length === 0) {
    return c.json({ error: "Announcement not found" }, 404);
  }

  const row = rows[0];
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
    200,
  );
});

// PATCH /announcements/:id — update an announcement (protected)
const updateAnnouncementRoute = createRoute({
  method: "patch",
  path: "/{id}",
  tags: ["Announcements"],
  summary: "Update an announcement",
  description: "Update an existing announcement. Requires bearer token authentication.",
  security: [{ Bearer: [] }],
  middleware: [authMiddleware()],
  request: {
    params: AnnouncementIdParam,
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

app.openapi(updateAnnouncementRoute, async (c) => {
  const { id } = c.req.valid("param");
  const body = c.req.valid("json");
  const db = c.get("db");

  // Only update fields that were actually sent in the body
  const updates: Record<string, unknown> = {};
  if (body.author !== undefined) updates.author = body.author;
  if (body.title !== undefined && body.title !== null) updates.title = body.title;
  if (body.content !== undefined) updates.content = body.content;
  if (body.archived_at !== undefined) updates.archivedAt = body.archived_at;
  if (body.level !== undefined && body.level !== null) updates.level = body.level;

  if (Object.keys(updates).length === 0) {
    // Nothing to update — just return the existing row
    const existing = await db.select().from(announcements).where(eq(announcements.id, id));
    if (existing.length === 0) {
      return c.json({ error: "Announcement not found" }, 404);
    }
    const row = existing[0];
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
      200,
    );
  }

  const result = await db
    .update(announcements)
    .set(updates)
    .where(eq(announcements.id, id))
    .returning();

  if (result.length === 0) {
    return c.json({ error: "Announcement not found" }, 404);
  }

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
    200,
  );
});

// DELETE /announcements/:id — delete an announcement (protected)
const deleteAnnouncementRoute = createRoute({
  method: "delete",
  path: "/{id}",
  tags: ["Announcements"],
  summary: "Delete an announcement",
  description: "Delete an announcement. Requires bearer token authentication.",
  security: [{ Bearer: [] }],
  middleware: [authMiddleware()],
  request: { params: AnnouncementIdParam },
  responses: {
    204: { description: "Announcement deleted" },
    401: { content: { "application/json": { schema: ErrorResponse } }, description: "Unauthorized" },
    403: { content: { "application/json": { schema: ErrorResponse } }, description: "Forbidden" },
    404: { content: { "application/json": { schema: ErrorResponse } }, description: "Announcement not found" },
  },
});

app.openapi(deleteAnnouncementRoute, async (c) => {
  const { id } = c.req.valid("param");
  const db = c.get("db");

  const result = await db.delete(announcements).where(eq(announcements.id, id)).returning();

  if (result.length === 0) {
    return c.json({ error: "Announcement not found" }, 404);
  }

  return c.body(null, 204);
});

export default app;
