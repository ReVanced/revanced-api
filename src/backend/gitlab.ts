import type {
    Backend,
    BackendRelease,
    BackendAsset,
    BackendContributor,
    BackendMember
} from './types';

interface GitLabAsset {
    name: string;
    direct_asset_url: string;
}

interface GitLabRelease {
    tag_name: string;
    description: string;
    created_at: string;
    upcoming_release: boolean;
    assets: {
        links: GitLabAsset[];
    };
}

interface GitLabContributor {
    name: string;
    avatar_url: string;
    web_url: string;
    commits: number;
}

interface GitLabMember {
    id: number;
    username: string;
    avatar_url: string;
    web_url: string;
    bio: string | null;
}

interface GitLabGpgKey {
    id: number;
}

import { formatDatetime } from '../utils';

function encodeProject(owner: string, repo: string): string {
    return encodeURIComponent(`${owner}/${repo}`);
}

export class GitLabBackend implements Backend {
    private readonly baseUrl: string;
    private readonly webUrl: string;
    private readonly headers: HeadersInit;

    constructor(url: string, token?: string) {
        this.webUrl = url;
        this.baseUrl = `${url}/api/v4`;
        const headers: Record<string, string> = {
            Accept: 'application/json'
        };
        if (token) {
            headers['PRIVATE-TOKEN'] = token;
        }
        this.headers = headers;
    }

    private async fetchJson<T>(url: string): Promise<T> {
        const response = await fetch(url, { headers: this.headers });
        if (!response.ok) {
            throw new Error(
                `GitLab API error: ${response.status} ${response.statusText} — ${url}`
            );
        }
        return response.json() as Promise<T>;
    }

    private mapRelease(release: GitLabRelease): BackendRelease {
        return {
            tag: release.tag_name,
            releaseNote: release.description ?? '',
            createdAt: formatDatetime(release.created_at),
            prerelease: release.upcoming_release,
            assets: release.assets.links.map(
                (asset): BackendAsset => ({
                    name: asset.name,
                    downloadUrl: asset.direct_asset_url
                })
            )
        };
    }

    async release(
        owner: string,
        repo: string,
        prerelease: boolean
    ): Promise<BackendRelease> {
        const project = encodeProject(owner, repo);

        if (prerelease) {
            const releases = await this.fetchJson<GitLabRelease[]>(
                `${this.baseUrl}/projects/${project}/releases?per_page=1`
            );
            if (releases.length === 0) {
                throw new Error(`No releases found for ${owner}/${repo}`);
            }
            return this.mapRelease(releases[0]);
        }

        const release = await this.fetchJson<GitLabRelease>(
            `${this.baseUrl}/projects/${project}/releases/permalink/latest`
        );
        return this.mapRelease(release);
    }

    async releases(
        owner: string,
        repo: string,
        count: number
    ): Promise<BackendRelease[]> {
        const project = encodeProject(owner, repo);
        const releases = await this.fetchJson<GitLabRelease[]>(
            `${this.baseUrl}/projects/${project}/releases?per_page=${count}`
        );

        return releases.map((release) => this.mapRelease(release));
    }

    async contributors(
        owner: string,
        repo: string
    ): Promise<BackendContributor[]> {
        const project = encodeProject(owner, repo);
        const contributors = await this.fetchJson<GitLabContributor[]>(
            `${this.baseUrl}/projects/${project}/repository/contributors?per_page=100`
        );

        return contributors.map((contributor) => ({
            name: contributor.name,
            avatarUrl: contributor.avatar_url,
            url: contributor.web_url,
            contributions: contributor.commits
        }));
    }

    async members(organization: string): Promise<BackendMember[]> {
        const groupMembers = await this.fetchJson<GitLabMember[]>(
            `${this.baseUrl}/groups/${encodeURIComponent(organization)}/members`
        );

        const members = await Promise.all(
            groupMembers.map(async (member) => {
                const gpgKeys = await this.fetchJson<GitLabGpgKey[]>(
                    `${this.baseUrl}/users/${member.id}/gpg_keys`
                );

                return {
                    name: member.username,
                    avatarUrl: member.avatar_url,
                    url: member.web_url,
                    bio: member.bio,
                    gpgKeys: {
                        ids: gpgKeys.map((key) => String(key.id)),
                        url: `${this.webUrl}/${member.username}.gpg`
                    }
                } satisfies BackendMember;
            })
        );

        return members;
    }

    repositoryUrl(owner: string, repo: string): string {
        return `${this.webUrl}/${owner}/${repo}`;
    }
}
