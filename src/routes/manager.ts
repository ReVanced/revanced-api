import { OpenAPIHono, createRoute } from '@hono/zod-openapi';
import type { Env } from '../types';
import { ErrorResponseSchema } from '../schemas/common';
import {
    ReleaseResponseSchema,
    VersionResponseSchema,
    HistoryResponseSchema
} from '../schemas/releases';
import * as managerService from '../services/manager';

const app = new OpenAPIHono<{ Bindings: Env }>();

app.openapi(
    createRoute({
        method: 'get',
        path: '/',
        tags: ['Manager'],
        summary: 'Get current manager release',
        description: 'Get the current stable manager release.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: ReleaseResponseSchema }
                },
                description: 'The latest manager release.'
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
        return c.json(await managerService.getRelease(c.env, false), 200);
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/prerelease',
        tags: ['Manager'],
        summary: 'Get current manager prerelease',
        description: 'Get the current manager prerelease.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: ReleaseResponseSchema }
                },
                description: 'The latest manager prerelease.'
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
        return c.json(await managerService.getRelease(c.env, true), 200);
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/version',
        tags: ['Manager'],
        summary: 'Get current manager release version',
        description: 'Get the current stable manager release version.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: VersionResponseSchema }
                },
                description: 'The current manager release version.'
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
        return c.json(await managerService.getVersion(c.env, false), 200);
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/version/prerelease',
        tags: ['Manager'],
        summary: 'Get current manager prerelease version',
        description: 'Get the current manager prerelease version.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: VersionResponseSchema }
                },
                description: 'The current manager prerelease version.'
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
        return c.json(await managerService.getVersion(c.env, true), 200);
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/history',
        tags: ['Manager'],
        summary: 'Get manager release history',
        description: 'Get the stable manager release history.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: HistoryResponseSchema }
                },
                description: 'The manager release history.'
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
        return c.json(await managerService.getHistory(c.env, false), 200);
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/history/prerelease',
        tags: ['Manager'],
        summary: 'Get manager prerelease history',
        description: 'Get the manager prerelease history.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: HistoryResponseSchema }
                },
                description: 'The manager prerelease history.'
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
        return c.json(await managerService.getHistory(c.env, true), 200);
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/downloaders',
        tags: ['Manager'],
        summary: 'Get current manager downloaders release',
        description: 'Get the current stable manager downloaders release.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: ReleaseResponseSchema }
                },
                description: 'The latest manager downloaders release.'
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
        return c.json(
            await managerService.getDownloadersRelease(c.env, false),
            200
        );
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/downloaders/prerelease',
        tags: ['Manager'],
        summary: 'Get current manager downloaders prerelease',
        description: 'Get the current manager downloaders prerelease.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: ReleaseResponseSchema }
                },
                description: 'The latest manager downloaders prerelease.'
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
        return c.json(
            await managerService.getDownloadersRelease(c.env, true),
            200
        );
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/downloaders/version',
        tags: ['Manager'],
        summary: 'Get current manager downloaders release version',
        description:
            'Get the current stable manager downloaders release version.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: VersionResponseSchema }
                },
                description: 'The current manager downloaders release version.'
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
        return c.json(
            await managerService.getDownloadersVersion(c.env, false),
            200
        );
    }
);

app.openapi(
    createRoute({
        method: 'get',
        path: '/downloaders/version/prerelease',
        tags: ['Manager'],
        summary: 'Get current manager downloaders prerelease version',
        description: 'Get the current manager downloaders prerelease version.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: VersionResponseSchema }
                },
                description:
                    'The current manager downloaders prerelease version.'
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
        return c.json(
            await managerService.getDownloadersVersion(c.env, true),
            200
        );
    }
);

export default app;
