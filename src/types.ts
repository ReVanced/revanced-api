import type { DrizzleD1Database } from "drizzle-orm/d1";
import type * as schema from "./db/schema";
import type { GitHubBackend } from "./backend/github";

export type Database = DrizzleD1Database<typeof schema>;

// Env bindings for Cloudflare Workers (D1 database bindings plus all the env vars)
export interface Env {
  // D1 database
  DB: D1Database;

  // Auth
  API_TOKEN: string;

  // GitHub
  GITHUB_TOKEN?: string;
  ORGANIZATION: string;

  // Repo config
  PATCHES_REPO: string;
  PATCHES_ASSET_REGEX: string;
  PATCHES_SIGNATURE_ASSET_REGEX: string;
  PATCHES_HISTORY_FILE: string;
  PATCHES_PUBLIC_KEY: string;
  MANAGER_REPO: string;
  MANAGER_ASSET_REGEX: string;
  MANAGER_DOWNLOADERS_REPO: string;
  MANAGER_DOWNLOADERS_ASSET_REGEX: string;
  MANAGER_HISTORY_FILE: string;

  // Branches
  MAIN_BRANCH: string;
  PRERELEASE_BRANCH: string;

  // Contributors
  CONTRIBUTORS_REPOS: string;

  // API
  API_VERSION: string;
}

// Parsed configuration that is computed once per request via middleware
export interface AppConfig {
  patchesAssetRegex: RegExp;
  patchesSignatureAssetRegex: RegExp;
  managerAssetRegex: RegExp;
  managerDownloadersAssetRegex: RegExp;
  contributorRepos: { repo: string; name: string }[];
}

// Variables set by middleware and available on all route handlers
export interface AppVariables {
  backend: GitHubBackend;
  db: Database;
  config: AppConfig;
}
