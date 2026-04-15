import type {
    Backend,
    BackendRelease,
    BackendAsset,
    BackendContributor,
    BackendMember
} from './types';

interface GitHubAsset {
    name: string;
    browser_download_url: string;
}

interface GitHubRelease {
    tag_name: string;
    body: string;
    created_at: string;
    prerelease: boolean;
    assets: GitHubAsset[];
}

interface GitHubContributor {
    login: string;
    avatar_url: string;
    html_url: string;
    contributions: number;
}

interface GitHubMember {
    login: string;
    avatar_url: string;
    html_url: string;
}

interface GitHubUser {
    login: string;
    avatar_url: string;
    html_url: string;
    bio: string | null;
}

interface GitHubGpgKey {
    key_id: string;
}

function formatDatetime(isoString: string): string {
    return isoString
        .replace(/\.\d{3}Z$/, '')
        .replace(/Z$/, '')
        .replace(/[+-]\d{2}:\d{2}$/, '');
}

export class GitHubBackend implements Backend {
    private readonly baseUrl = 'https://api.github.com';
    private readonly headers: HeadersInit;

    constructor(token?: string) {
        const headers: Record<string, string> = {
            Accept: 'application/vnd.github+json',
            'User-Agent': 'revanced-api'
        };
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        this.headers = headers;
    }

    private async fetchJson<T>(url: string): Promise<T> {
        const response = await fetch(url, { headers: this.headers });
        if (!response.ok) {
            throw new Error(
                `GitHub API error: ${response.status} ${response.statusText} — ${url}`
            );
        }
        return response.json() as Promise<T>;
    }

    async release(
        owner: string,
        repo: string,
        prerelease: boolean
    ): Promise<BackendRelease> {
        let release: GitHubRelease;

        if (prerelease) {
            const releases = await this.fetchJson<GitHubRelease[]>(
                `${this.baseUrl}/repos/${owner}/${repo}/releases?per_page=1`
            );
            if (releases.length === 0) {
                throw new Error(`No releases found for ${owner}/${repo}`);
            }
            release = releases[0];
        } else {
            release = await this.fetchJson<GitHubRelease>(
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
        const releases = await this.fetchJson<GitHubRelease[]>(
            `${this.baseUrl}/repos/${owner}/${repo}/releases?per_page=${count}`
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
        owner: string,
        repo: string
    ): Promise<BackendContributor[]> {
        const contributors = await this.fetchJson<GitHubContributor[]>(
            `${this.baseUrl}/repos/${owner}/${repo}/contributors?per_page=100`
        );

        return contributors.map((contributor) => ({
            name: contributor.login,
            avatarUrl: contributor.avatar_url,
            url: contributor.html_url,
            contributions: contributor.contributions
        }));
    }

    async members(organization: string): Promise<BackendMember[]> {
        const publicMembers = await this.fetchJson<GitHubMember[]>(
            `${this.baseUrl}/orgs/${organization}/public_members`
        );

        const members = await Promise.all(
            publicMembers.map(async (member) => {
                const [user, gpgKeys] = await Promise.all([
                    this.fetchJson<GitHubUser>(
                        `${this.baseUrl}/users/${member.login}`
                    ),
                    this.fetchJson<GitHubGpgKey[]>(
                        `${this.baseUrl}/users/${member.login}/gpg_keys`
                    )
                ]);

                return {
                    name: user.login,
                    avatarUrl: user.avatar_url,
                    url: user.html_url,
                    bio: user.bio,
                    gpgKeys: {
                        ids: gpgKeys.map((key) => key.key_id),
                        url: `https://github.com/${user.login}.gpg`
                    }
                } satisfies BackendMember;
            })
        );

        return members;
    }

    repositoryUrl(owner: string, repo: string): string {
        return `https://github.com/${owner}/${repo}`;
    }
}
