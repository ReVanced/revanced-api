import type {
    Backend,
    BackendRelease,
    BackendAsset,
    BackendContributor,
    BackendMember
} from './types';

interface GiteaAsset {
    name: string;
    browser_download_url: string;
}

interface GiteaRelease {
    tag_name: string;
    body: string;
    created_at: string;
    prerelease: boolean;
    assets: GiteaAsset[];
}

interface GiteaContributor {
    login: string;
    avatar_url: string;
    html_url: string;
    contributions: number;
}

interface GiteaMember {
    login: string;
    avatar_url: string;
}

interface GiteaUser {
    login: string;
    avatar_url: string;
    biography: string;
}

interface GiteaGpgKey {
    key_id: string;
}

import { formatDatetime } from '../utils';

export class GiteaBackend implements Backend {
    private readonly baseUrl: string;
    private readonly headers: HeadersInit;

    constructor(url: string, token?: string) {
        this.baseUrl = `${url}/api/v1`;
        const headers: Record<string, string> = {
            Accept: 'application/json'
        };
        if (token) {
            headers['Authorization'] = `token ${token}`;
        }
        this.headers = headers;
    }

    private async fetchJson<T>(url: string): Promise<T> {
        const response = await fetch(url, { headers: this.headers });
        if (!response.ok) {
            throw new Error(
                `Gitea API error: ${response.status} ${response.statusText} — ${url}`
            );
        }
        return response.json() as Promise<T>;
    }

    async release(
        owner: string,
        repo: string,
        prerelease: boolean
    ): Promise<BackendRelease> {
        let release: GiteaRelease;

        if (prerelease) {
            const releases = await this.fetchJson<GiteaRelease[]>(
                `${this.baseUrl}/repos/${owner}/${repo}/releases?limit=1`
            );
            if (releases.length === 0) {
                throw new Error(`No releases found for ${owner}/${repo}`);
            }
            release = releases[0];
        } else {
            release = await this.fetchJson<GiteaRelease>(
                `${this.baseUrl}/repos/${owner}/${repo}/releases/latest`
            );
        }

        return {
            tag: release.tag_name,
            releaseNote: release.body ?? '',
            createdAt: formatDatetime(release.created_at),
            prerelease: release.prerelease,
            assets: release.assets.map(
                (asset): BackendAsset => ({
                    name: asset.name,
                    downloadUrl: asset.browser_download_url
                })
            )
        };
    }

    async releases(
        owner: string,
        repo: string,
        count: number
    ): Promise<BackendRelease[]> {
        const releases = await this.fetchJson<GiteaRelease[]>(
            `${this.baseUrl}/repos/${owner}/${repo}/releases?limit=${count}`
        );

        return releases.map((release) => ({
            tag: release.tag_name,
            releaseNote: release.body ?? '',
            createdAt: formatDatetime(release.created_at),
            prerelease: release.prerelease,
            assets: release.assets.map(
                (asset): BackendAsset => ({
                    name: asset.name,
                    downloadUrl: asset.browser_download_url
                })
            )
        }));
    }

    async contributors(
        _owner: string,
        _repo: string
    ): Promise<BackendContributor[]> {
        // TODO: Forgejo does not have a contributors API yet.
        return [];
    }

    async members(organization: string): Promise<BackendMember[]> {
        const publicMembers = await this.fetchJson<GiteaMember[]>(
            `${this.baseUrl}/orgs/${organization}/public_members`
        );

        const members = await Promise.all(
            publicMembers.map(async (member) => {
                const [user, gpgKeys] = await Promise.all([
                    this.fetchJson<GiteaUser>(
                        `${this.baseUrl}/users/${member.login}`
                    ),
                    this.fetchJson<GiteaGpgKey[]>(
                        `${this.baseUrl}/users/${member.login}/gpg_keys`
                    )
                ]);

                return {
                    name: user.login,
                    avatarUrl: user.avatar_url,
                    url: `${this.baseUrl.replace('/api/v1', '')}/${user.login}`,
                    bio: user.biography || null,
                    gpgKeys: {
                        ids: gpgKeys.map((key) => key.key_id),
                        url: `${this.baseUrl}/users/${user.login}/gpg_keys`
                    }
                } satisfies BackendMember;
            })
        );

        return members;
    }

    repositoryUrl(owner: string, repo: string): string {
        return `${this.baseUrl.replace('/api/v1', '')}/${owner}/${repo}`;
    }
}
