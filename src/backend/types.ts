// Abstract data source for the API.
// Implement this to swap GitHub for GitLab, Gitea, or any other provider.

export interface BackendRelease {
    tag: string;
    releaseNote: string;
    createdAt: string; // ISO 8601 datetime without timezone suffix.
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
    release(
        owner: string,
        repo: string,
        prerelease: boolean
    ): Promise<BackendRelease>;
    releases(
        owner: string,
        repo: string,
        count: number
    ): Promise<BackendRelease[]>;
    contributors(owner: string, repo: string): Promise<BackendContributor[]>;
    members(organization: string): Promise<BackendMember[]>;
    repositoryUrl(owner: string, repo: string): string;
}
