import { getDatabase } from "../db/client";
import { announcements } from "../db/schema";
import { eq, desc } from "drizzle-orm";
import type { Env } from "../types";

function formatRow(row: typeof announcements.$inferSelect) {
	return {
		id: row.id,
		author: row.author,
		title: row.title,
		content: row.content,
		created_at: row.createdAt,
		archived_at: row.archivedAt,
		level: row.level,
	};
}

export async function listAnnouncements(env: Env) {
	const database = getDatabase(env.DB);
	const rows = await database
		.select()
		.from(announcements)
		.orderBy(desc(announcements.id));
	return rows.map(formatRow);
}

export async function createAnnouncement(
	env: Env,
	body: {
		author?: string;
		title: string;
		content?: string;
		created_at?: string;
		archived_at?: string;

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
			archivedAt: body.archived_at ?? null,
			level: body.level ?? 0,
		})
		.returning();

	return formatRow(result[0]);
}

export async function updateAnnouncement(
	env: Env,
	id: number,
	body: {
		author?: string;
		title?: string | null;
		content?: string | null;
		created_at?: string | null;
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

	if (Object.keys(updates).length === 0) {
		const database = getDatabase(env.DB);
		const rows = await database
			.select()
			.from(announcements)
			.where(eq(announcements.id, id));
		if (rows.length === 0) return null;
		return formatRow(rows[0]);
	}

	const result = await database
		.update(announcements)
		.set(updates)
		.where(eq(announcements.id, id))
		.returning();

	if (result.length === 0) return null;
	return formatRow(result[0]);
}

export async function deleteAnnouncement(env: Env, id: number) {
	const database = getDatabase(env.DB);
	const result = await database
		.delete(announcements)
		.where(eq(announcements.id, id))
		.returning();
	return result.length > 0;
}
