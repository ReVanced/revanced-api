import { OpenAPIHono, createRoute } from '@hono/zod-openapi';
import type { Env } from '../types';
import { authMiddleware } from '../auth/auth';
import { ErrorResponseSchema } from '../schemas/common';
import {
    AnnouncementIdParamSchema,
    AnnouncementResponseSchema,
    AnnouncementsResponseSchema,
    CreateAnnouncementBodySchema,
    LatestAnnouncementIdsResponseSchema,
    LatestAnnouncementsResponseSchema,
    UpdateAnnouncementBodySchema
} from '../schemas/announcements';
import * as announcementsService from '../services/announcements';

const app = new OpenAPIHono<{ Bindings: Env }>();

app.openapi(
    createRoute({
        method: 'get',
        path: '/',
        tags: ['Announcements'],
        summary: 'Get all announcements',
        description: 'Get all announcements ordered by newest first.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: AnnouncementsResponseSchema }
                },
                description: 'All announcements.'
            }
        }
    }),
    async (c) => {
        return c.json(await announcementsService.listAnnouncements(c.env), 200);
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/latest',
        tags: ['Announcements'],
        summary: 'Get the latest announcement for each tag',
        description:
            'Get the newest announcement for every available announcement tag.',
        responses: {
            200: {
                content: {
                    'application/json': {
                        schema: LatestAnnouncementsResponseSchema
                    }
                },
                description: 'The newest announcement for each tag.'
            }
        }
    }),
    async (c) => {
        return c.json(
            await announcementsService.getLatestAnnouncementsByTag(c.env),
            200
        );
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/latest/id',
        tags: ['Announcements'],
        summary: 'Get the latest announcement ID for each tag',
        description:
            'Get the ID of the newest announcement for every available announcement tag.',
        responses: {
            200: {
                content: {
                    'application/json': {
                        schema: LatestAnnouncementIdsResponseSchema
                    }
                },
                description: 'The newest announcement ID for each tag.'
            }
        }
    }),
    async (c) => {
        return c.json(
            await announcementsService.getLatestAnnouncementIdsByTag(c.env),
            200
        );
    }
);

app.openapi(
    createRoute({
        method: 'post',
        path: '/',
        tags: ['Announcements'],
        summary: 'Create an announcement',
        description:
            'Create a new announcement. Requires bearer token authentication.',
        security: [{ Bearer: [] }],
        middleware: [authMiddleware],
        request: {
            body: {
                content: {
                    'application/json': { schema: CreateAnnouncementBodySchema }
                }
            }
        },
        responses: {
            201: {
                content: {
                    'application/json': { schema: AnnouncementResponseSchema }
                },
                description: 'The created announcement.'
            },
            401: {
                content: {
                    'application/json': { schema: ErrorResponseSchema }
                },
                description: 'Unauthorized.'
            },
            403: {
                content: {
                    'application/json': { schema: ErrorResponseSchema }
                },
                description: 'Forbidden.'
            }
        }
    }),
    async (c) => {
        const body = c.req.valid('json');
        return c.json(
            await announcementsService.createAnnouncement(c.env, body),
            201
        );
    }
);

app.openapi(
    createRoute({
        method: 'patch',
        path: '/{id}',
        tags: ['Announcements'],
        summary: 'Update an announcement',
        description:
            'Update an existing announcement. Requires bearer token authentication.',
        security: [{ Bearer: [] }],
        middleware: [authMiddleware],
        request: {
            params: AnnouncementIdParamSchema,
            body: {
                content: {
                    'application/json': { schema: UpdateAnnouncementBodySchema }
                }
            }
        },
        responses: {
            200: {
                content: {
                    'application/json': { schema: AnnouncementResponseSchema }
                },
                description: 'The updated announcement.'
            },
            401: {
                content: {
                    'application/json': { schema: ErrorResponseSchema }
                },
                description: 'Unauthorized.'
            },
            403: {
                content: {
                    'application/json': { schema: ErrorResponseSchema }
                },
                description: 'Forbidden.'
            },
            404: {
                content: {
                    'application/json': { schema: ErrorResponseSchema }
                },
                description: 'Announcement not found.'
            }
        }
    }),
    async (c) => {
        const { id } = c.req.valid('param');
        const body = c.req.valid('json');
        const result = await announcementsService.updateAnnouncement(
            c.env,
            id,
            body
        );
        if (!result) {
            return c.json({ error: 'Announcement not found' }, 404);
        }
        return c.json(result, 200);
    }
);

app.openapi(
    createRoute({
        method: 'delete',
        path: '/{id}',
        tags: ['Announcements'],
        summary: 'Delete an announcement',
        description:
            'Delete an announcement. Requires bearer token authentication.',
        security: [{ Bearer: [] }],
        middleware: [authMiddleware],
        request: { params: AnnouncementIdParamSchema },
        responses: {
            204: { description: 'Announcement deleted.' },
            401: {
                content: {
                    'application/json': { schema: ErrorResponseSchema }
                },
                description: 'Unauthorized.'
            },
            403: {
                content: {
                    'application/json': { schema: ErrorResponseSchema }
                },
                description: 'Forbidden.'
            },
            404: {
                content: {
                    'application/json': { schema: ErrorResponseSchema }
                },
                description: 'Announcement not found.'
            }
        }
    }),
    async (c) => {
        const { id } = c.req.valid('param');
        const deleted = await announcementsService.deleteAnnouncement(
            c.env,
            id
        );
        if (!deleted) {
            return c.json({ error: 'Announcement not found' }, 404);
        }
        return c.body(null, 204);
    }
);

export default app;
