import { getDatabase } from "../db/client";
import { announcements, tags, announcementTags } from "../db/schema";
import { eq, desc, inArray } from "drizzle-orm";
import type { Env } from "../types";

async function getTagsForAnnouncement(db: ReturnType<typeof getDatabase>, announcementId: number): Promise<string[]> {
  const rows = await db
    .select({ name: tags.name })
    .from(announcementTags)
    .innerJoin(tags, eq(announcementTags.tagId, tags.id))
    .where(eq(announcementTags.announcementId, announcementId));
  return rows.map((r) => r.name);
}

async function syncTags(db: ReturnType<typeof getDatabase>, announcementId: number, tagNames: string[]) {
  // Get existing tag IDs for this announcement before removing
  const oldTagRows = await db
    .select({ tagId: announcementTags.tagId })
    .from(announcementTags)
    .where(eq(announcementTags.announcementId, announcementId));
  const oldTagIds = oldTagRows.map((r) => r.tagId);

  // Remove existing tags for this announcement
  await db.delete(announcementTags).where(eq(announcementTags.announcementId, announcementId));

  if (tagNames.length > 0) {
    // Ensure all tag names exist
    for (const name of tagNames) {
      await db.insert(tags).values({ name }).onConflictDoNothing();
    }

    // Get tag IDs
    const tagRows = await db.select().from(tags).where(inArray(tags.name, tagNames));

    // Insert announcement-tag associations
    if (tagRows.length > 0) {
      await db.insert(announcementTags).values(
        tagRows.map((t) => ({ announcementId, tagId: t.id })),
      );
    }
  }

  // Clean up orphaned tags from the old set
  if (oldTagIds.length > 0) {
    const usedTagIds = await db
      .selectDistinct({ tagId: announcementTags.tagId })
      .from(announcementTags)
      .where(inArray(announcementTags.tagId, oldTagIds));
    const stillUsedIds = new Set(usedTagIds.map((r) => r.tagId));
    const orphanedIds = oldTagIds.filter((id) => !stillUsedIds.has(id));
    if (orphanedIds.length > 0) {
      await db.delete(tags).where(inArray(tags.id, orphanedIds));
    }
  }
}

function formatRow(row: typeof announcements.$inferSelect, announcementTagNames: string[] = []) {
  return {
    id: row.id,
    author: row.author,
    title: row.title,
    content: row.content,
    tags: announcementTagNames,
    created_at: row.createdAt,
    archived_at: row.archivedAt,
    level: row.level,
  };
}

export async function listAnnouncements(env: Env) {
  const database = getDatabase(env.DB);
  const rows = await database.select().from(announcements).orderBy(desc(announcements.id));

  const results = [];
  for (const row of rows) {
    const tagNames = await getTagsForAnnouncement(database, row.id);
    results.push(formatRow(row, tagNames));
  }
  return results;
}

export async function createAnnouncement(
  env: Env,
  body: { author?: string; title: string; content?: string; tags?: string[]; level?: number },
) {
  const database = getDatabase(env.DB);
  const result = await database
    .insert(announcements)
    .values({
      author: body.author ?? null,
      title: body.title,
      content: body.content ?? null,
      createdAt: new Date().toISOString(),
      level: body.level ?? 0,
    })
    .returning();

  const created = result[0];

  if (body.tags && body.tags.length > 0) {
    await syncTags(database, created.id, body.tags);
  }

  const tagNames = await getTagsForAnnouncement(database, created.id);
  return formatRow(created, tagNames);
}

export async function updateAnnouncement(
  env: Env,
  id: number,
  body: {
    author?: string;
    title?: string | null;
    content?: string | null;
    tags?: string[];
    archived_at?: string | null;
    level?: number | null;
  },
) {
  const database = getDatabase(env.DB);

  const updates: Record<string, unknown> = {};
  if (body.author) updates.author = body.author;
  if (body.title) updates.title = body.title;
  if (body.content) updates.content = body.content;
  if (body.archived_at) updates.archivedAt = body.archived_at;
  if (body.level) updates.level = body.level;

  if (Object.keys(updates).length === 0 && body.tags === undefined) {
    const rows = await database.select().from(announcements).where(eq(announcements.id, id));
    if (rows.length === 0) return null;
    const tagNames = await getTagsForAnnouncement(database, rows[0].id);
    return formatRow(rows[0], tagNames);
  }

  let result;
  if (Object.keys(updates).length > 0) {
    result = await database
      .update(announcements)
      .set(updates)
      .where(eq(announcements.id, id))
      .returning();
  } else {
    const rows = await database.select().from(announcements).where(eq(announcements.id, id));
    result = rows;
  }

  if (result.length === 0) return null;

  if (body.tags !== undefined) {
    await syncTags(database, id, body.tags);
  }

  const tagNames = await getTagsForAnnouncement(database, id);
  return formatRow(result[0], tagNames);
}

export async function deleteAnnouncement(env: Env, id: number) {
  const database = getDatabase(env.DB);

  // Get tags associated with this announcement before deleting
  const announcementTagRows = await database
    .select({ tagId: announcementTags.tagId })
    .from(announcementTags)
    .where(eq(announcementTags.announcementId, id));
  const tagIds = announcementTagRows.map((r) => r.tagId);

  const result = await database.delete(announcements).where(eq(announcements.id, id)).returning();
  if (result.length === 0) return false;

  // Clean up orphaned tags (tags no longer referenced by any announcement)
  if (tagIds.length > 0) {
    const usedTagIds = await database
      .selectDistinct({ tagId: announcementTags.tagId })
      .from(announcementTags)
      .where(inArray(announcementTags.tagId, tagIds));
    const stillUsedIds = new Set(usedTagIds.map((r) => r.tagId));
    const orphanedIds = tagIds.filter((id) => !stillUsedIds.has(id));
    if (orphanedIds.length > 0) {
      await database.delete(tags).where(inArray(tags.id, orphanedIds));
    }
  }

  return true;
}
