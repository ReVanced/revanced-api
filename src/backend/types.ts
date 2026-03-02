// Backend interface — abstract data source for the API.
// Implement this to swap GitHub for GitLab, Gitea, or any other provider.

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

export interface Backend {
  // Gets a release from a repo (prerelease=true gets first release, false gets latest stable)
  release(owner: string, repo: string, prerelease: boolean): Promise<BackendRelease>;

  // Gets raw file content from a repo
  fileContent(owner: string, repo: string, branch: string, path: string): Promise<string>;

  // Gets contributors for a repo
  contributors(owner: string, repo: string): Promise<BackendContributor[]>;

  // Gets public members of an org
  members(org: string): Promise<BackendMember[]>;
}
