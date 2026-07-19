import { getDatabase } from '../db/client';
import { announcements, tags, announcementTags } from '../db/schema';
import { eq, desc, and, count, inArray, isNull } from 'drizzle-orm';
import type { Env } from '../types';

async function getTagsForAnnouncement(
    db: ReturnType<typeof getDatabase>,
    announcementId: number
): Promise<string[]> {
    const rows = await db
        .select({ name: tags.name })
        .from(announcementTags)
        .innerJoin(tags, eq(announcementTags.tagId, tags.id))
        .where(eq(announcementTags.announcementId, announcementId));
    return rows.map((r) => r.name);
}

async function getTagsForAnnouncements(
    db: ReturnType<typeof getDatabase>,
    announcementIds: number[]
): Promise<Map<number, string[]>> {
    if (announcementIds.length === 0) {
        return new Map();
    }

    const rows = await db
        .select({
            announcementId: announcementTags.announcementId,
            name: tags.name
        })
        .from(announcementTags)
        .innerJoin(tags, eq(announcementTags.tagId, tags.id))
        .where(inArray(announcementTags.announcementId, announcementIds));

    const tagMap = new Map<number, string[]>();
    for (const row of rows) {
        const announcementTagNames = tagMap.get(row.announcementId);
        if (announcementTagNames) {
            announcementTagNames.push(row.name);
            continue;
        }

        tagMap.set(row.announcementId, [row.name]);
    }

    return tagMap;
}

async function getLatestAnnouncementRowsByTag(
    db: ReturnType<typeof getDatabase>
) {
    const rows = await db
        .select({ tagName: tags.name, announcement: announcements })
        .from(tags)
        .innerJoin(announcementTags, eq(announcementTags.tagId, tags.id))
        .innerJoin(
            announcements,
            eq(announcementTags.announcementId, announcements.id)
        )
        .orderBy(tags.name, desc(announcements.id));

    const latestByTag = new Map<string, typeof announcements.$inferSelect>();
    for (const row of rows) {
        if (!latestByTag.has(row.tagName)) {
            latestByTag.set(row.tagName, row.announcement);
        }
    }

    return latestByTag;
}

async function getLatestUntaggedAnnouncementRow(
    db: ReturnType<typeof getDatabase>
) {
    const [row] = await db
        .select({ announcement: announcements })
        .from(announcements)
        .leftJoin(
            announcementTags,
            eq(announcementTags.announcementId, announcements.id)
        )
        .where(isNull(announcementTags.announcementId))
        .orderBy(desc(announcements.id))
        .limit(1);

    return row?.announcement ?? null;
}

/** Find or create a tag by name, returning its ID. */
async function findOrCreateTag(
    db: ReturnType<typeof getDatabase>,
    name: string
): Promise<number> {
    await db.insert(tags).values({ name }).onConflictDoNothing();
    const [row] = await db
        .select({ id: tags.id })
        .from(tags)
        .where(eq(tags.name, name));
    return row.id;
}

function formatRow(
    row: typeof announcements.$inferSelect,
    announcementTagNames: string[] = []
) {
    return {
        id: row.id,
        author: row.author,
        title: row.title,
        content: row.content,
        tags: announcementTagNames,
        created_at: row.createdAt,
        archived_at: row.archivedAt,
        level: row.level
    };
}

type LatestAnnouncementEntry = {
    tag: string | null;
    announcement: ReturnType<typeof formatRow>;
};

type LatestAnnouncementIdEntry = {
    tag: string | null;
    id: number;
};

export async function listAnnouncements(env: Env) {
    const database = getDatabase(env.DB);
    const rows = await database
        .select()
        .from(announcements)
        .orderBy(desc(announcements.id));

    const results = [];
    for (const row of rows) {
        const tagNames = await getTagsForAnnouncement(database, row.id);
        results.push(formatRow(row, tagNames));
    }
    return results;
}

export async function getLatestAnnouncementsByTag(env: Env) {
    const database = getDatabase(env.DB);
    const latestByTag = await getLatestAnnouncementRowsByTag(database);
    const latestUntagged = await getLatestUntaggedAnnouncementRow(database);
    const announcementIds = [
        ...new Set([...latestByTag.values()].map((row) => row.id))
    ];
    if (latestUntagged) {
        announcementIds.push(latestUntagged.id);
    }
    const announcementTagsMap = await getTagsForAnnouncements(database, [
        ...new Set(announcementIds)
    ]);

    const entries: LatestAnnouncementEntry[] = [...latestByTag.entries()].map(
        ([tagName, row]) => ({
            tag: tagName,
            announcement: formatRow(row, announcementTagsMap.get(row.id) ?? [])
        })
    );

    if (latestUntagged) {
        entries.push({
            tag: null,
            announcement: formatRow(latestUntagged, [])
        });
    }

    return entries;
}

export async function getLatestAnnouncementIdsByTag(env: Env) {
    const database = getDatabase(env.DB);
    const latestByTag = await getLatestAnnouncementRowsByTag(database);
    const latestUntagged = await getLatestUntaggedAnnouncementRow(database);

    const entries: LatestAnnouncementIdEntry[] = [...latestByTag.entries()].map(
        ([tagName, row]) => ({
            tag: tagName,
            id: row.id
        })
    );

    if (latestUntagged) {
        entries.push({ tag: null, id: latestUntagged.id });
    }

    return entries;
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
    }
) {
    const database = getDatabase(env.DB);
    const result = await database
        .insert(announcements)
        .values({
            author: body.author ?? null,
            title: body.title,
            content: body.content ?? null,
            createdAt: body.created_at ?? new Date().toISOString(),
            level: body.level ?? 0
        })
        .returning();

    const created = result[0];

    if (body.tags && body.tags.length > 0) {
        const tagIds: number[] = [];
        for (const name of body.tags) {
            tagIds.push(await findOrCreateTag(database, name));
        }
        await database
            .insert(announcementTags)
            .values(
                tagIds.map((tagId) => ({ announcementId: created.id, tagId }))
            );
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
    }
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
        const rows = await database
            .select()
            .from(announcements)
            .where(eq(announcements.id, id));
        if (rows.length === 0) return null;
        row = rows[0];
    }

    if (body.tags !== undefined) {
        // Get current tags for this announcement
        const currentTagRows = await database
            .select({ tagId: announcementTags.tagId, name: tags.name })
            .from(announcementTags)
            .innerJoin(tags, eq(announcementTags.tagId, tags.id))
            .where(eq(announcementTags.announcementId, id));

        const currentTagNames = new Set(currentTagRows.map((r) => r.name));
        const newTagNames = new Set(body.tags);

        // Add tags that are in the new set but not currently associated
        for (const name of body.tags) {
            if (!currentTagNames.has(name)) {
                const tagId = await findOrCreateTag(database, name);
                await database
                    .insert(announcementTags)
                    .values({ announcementId: id, tagId });
            }
        }

        // Remove tags no longer needed and delete orphaned tag records
        for (const { tagId, name } of currentTagRows) {
            if (newTagNames.has(name)) continue;

            await database
                .delete(announcementTags)
                .where(
                    and(
                        eq(announcementTags.announcementId, id),
                        eq(announcementTags.tagId, tagId)
                    )
                );

            const [usage] = await database
                .select({ count: count() })
                .from(announcementTags)
                .where(eq(announcementTags.tagId, tagId));
            if (usage.count === 0) {
                await database.delete(tags).where(eq(tags.id, tagId));
            }
        }
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

    const result = await database
        .delete(announcements)
        .where(eq(announcements.id, id))
        .returning();
    if (result.length === 0) return false;

    // Delete tags that are no longer referenced by any announcement
    for (const tagId of tagIds) {
        const [usage] = await database
            .select({ count: count() })
            .from(announcementTags)
            .where(eq(announcementTags.tagId, tagId));
        if (usage.count === 0) {
            await database.delete(tags).where(eq(tags.id, tagId));
        }
    }

    return true;
}
