import type { MiddlewareHandler } from 'hono';
import type { Env } from '../types';

// Checks the Authorization header against the API_TOKEN env var.
export const authMiddleware: MiddlewareHandler<{ Bindings: Env }> = async (
    c,
    next
) => {
    const authHeader = c.req.header('Authorization');

    if (!authHeader) {
        return c.json({ error: 'Missing Authorization header' }, 401);
    }

    const [scheme, token] = authHeader.split(' ', 2);

    if (scheme !== 'Bearer' || !token) {
        return c.json(
            {
                error: 'Invalid Authorization header format. Expected: Bearer <token>'
            },
            401
        );
    }

    if (token !== c.env.API_TOKEN) {
        return c.json({ error: 'Invalid token' }, 403);
    }

    await next();
};
