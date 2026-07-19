import { OpenAPIHono, createRoute } from '@hono/zod-openapi';
import type { Env } from '../types';
import { ErrorResponseSchema } from '../schemas/common';
import { TeamResponseSchema } from '../schemas/contributors';
import * as teamService from '../services/team';
import { cacheControl, CacheDuration } from '../cache';

const app = new OpenAPIHono<{ Bindings: Env }>();

// 1-day cache for team members
app.use('*', cacheControl(CacheDuration.day));

app.openapi(
    createRoute({
        method: 'get',
        path: '/',
        tags: ['API'],
        summary: 'Get team members',
        description: 'Get the list of team members from the organization.',
        responses: {
            200: {
                content: { 'application/json': { schema: TeamResponseSchema } },
                description: 'The list of team members.'
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
        return c.json(await teamService.getTeamMembers(c.env), 200);
    }
);

export default app;
