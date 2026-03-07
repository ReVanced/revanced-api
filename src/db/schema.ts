import { sqliteTable, integer, text } from "drizzle-orm/sqlite-core";

export const announcements = sqliteTable("announcements", {
	id: integer("id").primaryKey({ autoIncrement: true }),
	author: text("author"),
	title: text("title").notNull(),
	content: text("content"),
	createdAt: text("created_at")
		.notNull()
		.$defaultFn(() => new Date().toISOString()),
	archivedAt: text("archived_at"),
	level: integer("level").notNull().default(0),
});
