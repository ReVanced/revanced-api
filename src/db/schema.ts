import {
    index,
    integer,
    primaryKey,
    sqliteTable,
    text
} from 'drizzle-orm/sqlite-core';

export const announcements = sqliteTable('announcements', {
    id: integer('id').primaryKey({ autoIncrement: true }),
    author: text('author'),
    title: text('title').notNull(),
    content: text('content'),
    createdAt: text('created_at')
        .notNull()
        .$defaultFn(() => new Date().toISOString()),
    archivedAt: text('archived_at'),
    level: integer('level').notNull().default(0)
});

export const tags = sqliteTable('tags', {
    id: integer('id').primaryKey({ autoIncrement: true }),
    name: text('name').notNull().unique()
});

export const announcementTags = sqliteTable(
    'announcement_tags',
    {
        announcementId: integer('announcement_id')
            .notNull()
            .references(() => announcements.id, { onDelete: 'cascade' }),
        tagId: integer('tag_id')
            .notNull()
            .references(() => tags.id, { onDelete: 'cascade' })
    },
    (table) => ({
        pk: primaryKey({ columns: [table.announcementId, table.tagId] }),
        tagIdIdx: index('announcement_tags_tag_id_idx').on(table.tagId)
    })
);
