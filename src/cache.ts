import type { MiddlewareHandler } from 'hono';

const SECONDS_PER_DAY = 86400;

/** Cache duration presets matching the original Kotlin API. */
export const CacheDuration = {
    /** 5 minutes — default for most API routes. */
    short: 5 * 60,
    /** 1 day — for contributors, team, about. */
    day: SECONDS_PER_DAY,
    /** 356 days — for public keys (essentially immutable). */
    immutable: 356 * SECONDS_PER_DAY
} as const;

/**
 * Hono middleware that sets a `Cache-Control` header.
 * Cloudflare's CDN will respect `max-age` for edge caching.
 */
export function cacheControl(maxAgeSeconds: number): MiddlewareHandler {
    const value = `public, max-age=${maxAgeSeconds}`;
    return async (c, next) => {
        c.header('Cache-Control', value);
        await next();
    };
}
