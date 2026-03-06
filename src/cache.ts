import { cache } from "hono/cache";
import type { MiddlewareHandler } from "hono";

const SECONDS_PER_DAY = 86400;

/** Cache duration presets matching the original Kotlin API. */
export const CacheDuration = {
	/** 5 minutes — default for most API routes. */
	short: 5 * 60,
	/** 1 day — for contributors, team, about. */
	day: SECONDS_PER_DAY,
	/** 356 days — for public keys (essentially immutable). */
	immutable: 356 * SECONDS_PER_DAY,
} as const;

/**
 * Edge caching middleware using Hono's built-in cache().
 * Sets Cache-Control headers that Cloudflare's CDN and the Cache API respect.
 */
export function edgeCache(cacheName: string, maxAgeSeconds: number): MiddlewareHandler {
	return cache({
		cacheName: `revanced-api-${cacheName}`,
		cacheControl: `public, max-age=${maxAgeSeconds}`,
	});
}
