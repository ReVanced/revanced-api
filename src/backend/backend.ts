/* backend interface (abstract data source for the api) -- implement this to swap github for gitlab gitea or whatever */

export interface BackendRelease {
  tag: string;
  releaseNote: string;
  createdAt: string; // ISO 8601 datetime without timezone suffix
  prerelease: boolean;
  assets: BackendAsset[];
}

export interface BackendAsset {
  name: string;
  downloadUrl: string;
}

export interface BackendContributor {
  name: string;
  avatarUrl: string;
  url: string;
  contributions: number;
}

export interface BackendMember {
  name: string;
  avatarUrl: string;
  url: string;
  bio: string | null;
  gpgKeys: {
    ids: string[];
    url: string;
  };
}

export interface BackendRateLimit {
  limit: number;
  remaining: number;
  reset: string; // ISO 8601 datetime without timezone suffix
}

export interface Backend {
  // gets a release from a repo (prerelease=true gets first release, false gets latest stable)
  release(owner: string, repo: string, prerelease: boolean): Promise<BackendRelease>;

  /* gets raw file content from a repo */
  fileContent(owner: string, repo: string, branch: string, path: string): Promise<string>;

  // gets contributors for a repo
  contributors(owner: string, repo: string): Promise<BackendContributor[]>;

  /* gets public members of an org */
  members(org: string): Promise<BackendMember[]>;

  // rate limit status of the backend api
  rateLimit(): Promise<BackendRateLimit | null>;
}
