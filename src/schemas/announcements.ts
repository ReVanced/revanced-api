import { z } from "@hono/zod-openapi";

export const AnnouncementResponse = z
  .object({
    id: z.number().int().openapi({ example: 1 }),
    author: z.string().nullable().openapi({ example: "ReVanced" }),
    title: z.string().openapi({ example: "Welcome" }),
    content: z.string().nullable().openapi({ example: "Some content" }),
    created_at: z.string().openapi({ example: "2025-01-01T00:00:00" }),
    archived_at: z.string().nullable().openapi({ example: null }),
    level: z.number().int().openapi({ example: 0 }),
  })
  .openapi("Announcement");

export const AnnouncementsResponse = z.array(AnnouncementResponse);

export const CreateAnnouncementBody = z
  .object({
    author: z.string().optional().openapi({ example: "ReVanced" }),
    title: z.string().openapi({ example: "Welcome" }),
    content: z.string().optional().openapi({ example: "Some content" }),
    level: z.number().int().optional().default(0).openapi({ example: 0 }),
  })
  .openapi("CreateAnnouncement");

export const UpdateAnnouncementBody = z
  .object({
    author: z.string().optional().openapi({ example: "ReVanced" }),
    title: z.string().optional().openapi({ example: "Updated title" }),
    content: z.string().nullable().optional().openapi({ example: "Updated content" }),
    archived_at: z.string().nullable().optional().openapi({ example: "2025-06-01T00:00:00" }),
    level: z.number().int().optional().openapi({ example: 1 }),
  })
  .openapi("UpdateAnnouncement");
