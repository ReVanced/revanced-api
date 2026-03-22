import { z } from '@hono/zod-openapi';

export const AnnouncementIdParamSchema = z.object({
    id: z.coerce
        .number()
        .int()
        .openapi({
            description: 'Announcement ID.',
            example: 1,
            param: { in: 'path', required: true }
        })
});

export const AnnouncementResponseSchema = z
    .object({
        id: z.number().int().openapi({ example: 1 }),
        author: z.string().nullable().openapi({ example: 'ReVanced' }),
        title: z.string().openapi({ example: 'Welcome' }),
        content: z.string().nullable().openapi({ example: 'Some content' }),
        tags: z.array(z.string()).openapi({ example: ['Important'] }),
        created_at: z.iso
            .datetime()
            .openapi({ example: '1970-01-01T00:00:00.000Z' }),
        archived_at: z.iso.datetime().nullable().openapi({ example: null }),
        level: z.number().int().openapi({ example: 0 })
    })
    .openapi('Announcement');

export const AnnouncementsResponseSchema = z.array(AnnouncementResponseSchema);

export const LatestAnnouncementEntrySchema = z
    .object({
        tag: z.string().nullable().openapi({ example: 'Important' }),
        announcement: AnnouncementResponseSchema
    })
    .openapi('LatestAnnouncementEntry');

export const LatestAnnouncementsResponseSchema = z
    .array(LatestAnnouncementEntrySchema)
    .openapi('LatestAnnouncementsByTag');

export const LatestAnnouncementIdEntrySchema = z
    .object({
        tag: z.string().nullable().openapi({ example: 'Important' }),
        id: z.number().int().openapi({ example: 1 })
    })
    .openapi('LatestAnnouncementIdEntry');

export const LatestAnnouncementIdsResponseSchema = z
    .array(LatestAnnouncementIdEntrySchema)
    .openapi('LatestAnnouncementIdsByTag');

export const CreateAnnouncementBodySchema = z
    .object({
        author: z.string().optional().openapi({ example: 'ReVanced' }),
        title: z.string().openapi({ example: 'Welcome' }),
        content: z.string().optional().openapi({ example: 'Some content' }),
        tags: z.array(z.string()).openapi({ example: ['Important'] }),
        created_at: z.string().datetime().nullable().optional().openapi({
            example: '1970-01-01T00:00:00.000Z',
            description: 'UTC timestamp. Defaults to current time if omitted.'
        }),
        archived_at: z.iso
            .datetime()
            .nullable()
            .optional()
            .openapi({ example: null, description: 'UTC timestamp.' }),
        level: z.number().int().optional().default(0).openapi({ example: 0 })
    })
    .openapi('CreateAnnouncement');

export const UpdateAnnouncementBodySchema = z
    .object({
        author: z.string().optional().openapi({ example: 'ReVanced' }),
        title: z.string().openapi({ example: 'Welcome' }),
        content: z.string().optional().openapi({ example: 'Some content' }),
        tags: z.array(z.string()).openapi({ example: ['Important'] }),
        created_at: z.iso.datetime().nullable().optional().openapi({
            example: '1970-01-01T00:00:00.000Z',
            description: 'UTC timestamp.'
        }),
        archived_at: z.iso
            .datetime()
            .nullable()
            .optional()
            .openapi({ example: null, description: 'UTC timestamp.' }),
        level: z.number().int().optional().default(0).openapi({ example: 0 })
    })
    .openapi('UpdateAnnouncement');
