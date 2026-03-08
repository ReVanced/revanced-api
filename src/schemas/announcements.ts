import { z } from "@hono/zod-openapi";

export const AnnouncementIdParamSchema = z.object({
	id: z.coerce
		.number()
		.int()
		.openapi({
			description: "Announcement ID.",
			example: 1,
			param: { in: "path", required: true },
		}),
});

export const AnnouncementResponseSchema = z
	.object({
		id: z.number().int().openapi({ example: 1 }),
		author: z.string().nullable().openapi({ example: "ReVanced" }),
		title: z.string().openapi({ example: "Welcome" }),
		content: z.string().nullable().openapi({ example: "Some content" }),
		tags: z.array(z.string()).openapi({ example: ["Important"] }),
		created_at: z.string().openapi({ example: "2025-01-01T00:00:00" }),
		archived_at: z.string().nullable().openapi({ example: null }),
		level: z.number().int().openapi({ example: 0 }),
	})
	.openapi("Announcement");

export const AnnouncementsResponseSchema = z.array(AnnouncementResponseSchema);

export const CreateAnnouncementBodySchema = z
	.object({
		author: z.string().optional().openapi({ example: "ReVanced" }),
		title: z.string().openapi({ example: "Welcome" }),
		content: z.string().optional().openapi({ example: "Some content" }),
		tags: z.array(z.string()).optional().openapi({ example: ["Important"] }),
		level: z.number().int().optional().default(0).openapi({ example: 0 }),
	})
	.openapi("CreateAnnouncement");

export const UpdateAnnouncementBodySchema = z
	.object({
		author: z.string().optional().openapi({ example: "ReVanced" }),
		title: z.string().openapi({ example: "Welcome" }),
		content: z.string().optional().openapi({ example: "Some content" }),
		created_at: z
			.string()
			.datetime()
			.nullable()
			.optional()
			.openapi({ example: "2024-01-01T00:00:00.000Z", description: "UTC timestamp." }),
		tags: z.array(z.string()).optional().openapi({ example: ["Important"] }),
		archived_at: z
			.string()
			.datetime()
			.nullable()
			.optional()
			.openapi({ example: null, description: "UTC timestamp." }),
		level: z.number().int().optional().default(0).openapi({ example: 0 }),
	})
	.openapi("UpdateAnnouncement");
