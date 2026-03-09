import { getDatabase } from "../db/client";
import { announcements, tags, announcementTags } from "../db/schema";
import { eq, desc, inArray, count } from "drizzle-orm";
import type { Env } from "../types";

async function getTagsForAnnouncement(db: ReturnType<typeof getDatabase>, announcementId: number): Promise<string[]> {
	const rows = await db
		.select({ name: tags.name })
		.from(announcementTags)
		.innerJoin(tags, eq(announcementTags.tagId, tags.id))
		.where(eq(announcementTags.announcementId, announcementId));
	return rows.map((r) => r.name);
}

/** Find or create a tag by name, returning its ID. */
async function findOrCreateTag(db: ReturnType<typeof getDatabase>, name: string): Promise<number> {
	await db.insert(tags).values({ name }).onConflictDoNothing();
	const [row] = await db.select({ id: tags.id }).from(tags).where(eq(tags.name, name));
	return row.id;
}

/** Delete tags that are no longer referenced by any announcement. */
async function deleteOrphanedTags(db: ReturnType<typeof getDatabase>, tagIds: number[]) {
	for (const tagId of tagIds) {
		const [usage] = await db
			.select({ count: count() })
			.from(announcementTags)
			.where(eq(announcementTags.tagId, tagId));
		if (usage.count === 0) {
			await db.delete(tags).where(eq(tags.id, tagId));
		}
	}
}

/**
 * Sync tags for an announcement: find or create each tag, replace associations,
 * and clean up orphaned tags from the old set.
 */
async function syncTags(db: ReturnType<typeof getDatabase>, announcementId: number, tagNames: string[]) {
	// Get the old tags before updating
	const oldTagRows = await db
		.select({ tagId: announcementTags.tagId })
		.from(announcementTags)
		.where(eq(announcementTags.announcementId, announcementId));
	const oldTagIds = oldTagRows.map((r) => r.tagId);

	// Remove existing associations
	await db.delete(announcementTags).where(eq(announcementTags.announcementId, announcementId));

	// Find or create each new tag and associate it
	const newTagIds: number[] = [];
	for (const name of tagNames) {
		const tagId = await findOrCreateTag(db, name);
		newTagIds.push(tagId);
	}

	if (newTagIds.length > 0) {
		await db.insert(announcementTags).values(
			newTagIds.map((tagId) => ({ announcementId, tagId })),
		);
	}

	// Delete old tags that are no longer referenced by any announcement
	const orphanCandidates = oldTagIds.filter((id) => !newTagIds.includes(id));
	if (orphanCandidates.length > 0) {
		await deleteOrphanedTags(db, orphanCandidates);
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
	body: {
		author?: string;
		title: string;
		content?: string;
		created_at?: string | null;
		tags?: string[];
		level?: number;
	},
) {
	const database = getDatabase(env.DB);
	const result = await database
		.insert(announcements)
		.values({
			author: body.author ?? null,
			title: body.title,
			content: body.content ?? null,
			createdAt: body.created_at ?? new Date().toISOString(),
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
		created_at?: string | null;
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
	if (body.created_at) updates.createdAt = body.created_at;
	if (body.archived_at) updates.archivedAt = body.archived_at;
	if (body.level) updates.level = body.level;

	let row;
	if (Object.keys(updates).length > 0) {
		const result = await database
			.update(announcements)
			.set(updates)
			.where(eq(announcements.id, id))
			.returning();
		if (result.length === 0) return null;
		row = result[0];
	} else {
		const rows = await database.select().from(announcements).where(eq(announcements.id, id));
		if (rows.length === 0) return null;
		row = rows[0];
	}

	if (body.tags !== undefined) {
		await syncTags(database, id, body.tags);
	}

	const tagNames = await getTagsForAnnouncement(database, id);
	return formatRow(row, tagNames);
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

	// Delete tags that are no longer referenced by any announcement
	if (tagIds.length > 0) {
		await deleteOrphanedTags(database, tagIds);
	}

	return true;
}
