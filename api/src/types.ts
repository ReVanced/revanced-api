import type { DrizzleD1Database } from 'drizzle-orm/d1';
import type * as schema from './db/schema';

export type Database = DrizzleD1Database<typeof schema>;

export interface Env {
    ASSETS: Fetcher;
    DB: D1Database;
    API_TOKEN: string;
    GITHUB_TOKEN?: string;
    ORGANIZATION: string;
    PATCHES_REPO: string;
    PATCHES_ASSET_REGEX: string;
    PATCHES_SIGNATURE_ASSET_REGEX: string;
    MANAGER_REPO: string;
    MANAGER_ASSET_REGEX: string;
    MANAGER_DOWNLOADERS_REPO: string;
    MANAGER_DOWNLOADERS_ASSET_REGEX: string;
    PATCHES_PUBLIC_KEY_FILE: string;
    CONTRIBUTORS_REPOS: string;
    API_VERSION: string;
}
