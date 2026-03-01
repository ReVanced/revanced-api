/* env bindings for cloudflare workers (d1 database bindings plus all the env vars) */
export interface Env {
  /* d1 db */
  DB: D1Database;

  // auth stuff
  API_TOKEN: string;

  /* github */ GITHUB_TOKEN?: string;
  ORGANIZATION: string;

  // repo config stuff
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

  /* branches */ MAIN_BRANCH: string;
  PRERELEASE_BRANCH: string;

  // contributors
  CONTRIBUTORS_REPOS: string;

  /* api stuff */
  API_VERSION: string;
}
