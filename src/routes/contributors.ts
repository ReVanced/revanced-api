import { OpenAPIHono, createRoute } from '@hono/zod-openapi';
import type { Env } from '../types';
import { ErrorResponseSchema } from '../schemas/common';
import { ContributorsResponseSchema } from '../schemas/contributors';
import * as contributorsService from '../services/contributors';
import { cacheControl, CacheDuration } from '../cache';

const app = new OpenAPIHono<{ Bindings: Env }>();

// 1-day cache for contributors
app.use('*', cacheControl(CacheDuration.day));

app.openapi(
    createRoute({
        method: 'get',
        path: '/',
        tags: ['API'],
        summary: 'Get contributors',
        description:
            'Get the list of contributors for each configured repository.',
        responses: {
            200: {
                content: {
                    'application/json': { schema: ContributorsResponseSchema }
                },
                description: 'The list of contributors.'
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
        return c.json(await contributorsService.getContributors(c.env), 200);
    }
);

export default app;
