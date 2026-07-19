import { OpenAPIHono, createRoute } from '@hono/zod-openapi';
import type { Env } from '../types';
import { ErrorResponseSchema } from '../schemas/common';
import {
    ReleaseResponseSchema,
    VersionResponseSchema,
    HistoryResponseSchema,
    PublicKeyResponseSchema
} from '../schemas/releases';
import * as patchesService from '../services/patches';

const app = new OpenAPIHono<{ Bindings: Env }>();

app.openapi(
    createRoute({
        method: 'get',
        path: '/',
        tags: ['Patches'],
        summary: 'Get current patches release',
        description: 'Get the current stable patches release.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: ReleaseResponseSchema }
                },
                description: 'The current patches release.'
            },
            500: {
                content: {
                    'application/json': { schema: ErrorResponseSchema }
                },
                description: 'GitHub API error.'
            }
        }
    }),
    async (c) => {
        return c.json(await patchesService.getRelease(c.env, false), 200);
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/prerelease',
        tags: ['Patches'],
        summary: 'Get current patches prerelease',
        description: 'Get the current patches prerelease.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: ReleaseResponseSchema }
                },
                description: 'The current patches prerelease.'
            },
            500: {
                content: {
                    'application/json': { schema: ErrorResponseSchema }
                },
                description: 'GitHub API error.'
            }
        }
    }),
    async (c) => {
        return c.json(await patchesService.getRelease(c.env, true), 200);
    }
);

// --- Version ---

app.openapi(
    createRoute({
        method: 'get',
        path: '/version',
        tags: ['Patches'],
        summary: 'Get current patches release version',
        description: 'Get the current stable patches release version.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: VersionResponseSchema }
                },
                description: 'The current patches release version.'
            },
            500: {
                content: {
                    'application/json': { schema: ErrorResponseSchema }
                },
                description: 'GitHub API error.'
            }
        }
    }),
    async (c) => {
        return c.json(await patchesService.getVersion(c.env, false), 200);
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/version/prerelease',
        tags: ['Patches'],
        summary: 'Get current patches prerelease version',
        description: 'Get the current patches prerelease version.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: VersionResponseSchema }
                },
                description: 'The current patches prerelease version.'
            },
            500: {
                content: {
                    'application/json': { schema: ErrorResponseSchema }
                },
                description: 'GitHub API error.'
            }
        }
    }),
    async (c) => {
        return c.json(await patchesService.getVersion(c.env, true), 200);
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/history',
        tags: ['Patches'],
        summary: 'Get patches release history',
        description: 'Get the stable patches release history.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: HistoryResponseSchema }
                },
                description: 'The patches release history.'
            },
            500: {
                content: {
                    'application/json': { schema: ErrorResponseSchema }
                },
                description: 'GitHub API error.'
            }
        }
    }),
    async (c) => {
        return c.json(await patchesService.getHistory(c.env, false), 200);
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/history/prerelease',
        tags: ['Patches'],
        summary: 'Get patches prerelease history',
        description: 'Get the patches prerelease history.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: HistoryResponseSchema }
                },
                description: 'The patches prerelease history.'
            },
            500: {
                content: {
                    'application/json': { schema: ErrorResponseSchema }
                },
                description: 'GitHub API error.'
            }
        }
    }),
    async (c) => {
        return c.json(await patchesService.getHistory(c.env, true), 200);
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/keys',
        tags: ['Patches'],
        summary: 'Get patches public keys',
        description: 'Get the public keys for verifying patches assets.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: PublicKeyResponseSchema }
                },
                description: 'The public keys.'
            }
        }
    }),
    async (c) => {
        return c.json(await patchesService.getPublicKey(c.env), 200);
    }
);

export default app;
