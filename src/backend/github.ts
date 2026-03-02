import type {
  Backend,
  BackendRelease,
  BackendAsset,
  BackendContributor,
  BackendMember,
} from "./types";

// GitHub API response types — all the shapes that GitHub sends back

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

// Formats an ISO 8601 datetime to bare datetime without timezone suffix or milliseconds.
// "2025-01-15T10:30:00.123Z" becomes "2025-01-15T10:30:00"
function formatDatetime(isoString: string): string {
  return isoString
    .replace(/\.\d{3}Z$/, "")
    .replace(/Z$/, "")
    .replace(/[+-]\d{2}:\d{2}$/, "");
}

// GitHubBackend class — implements the Backend interface using the GitHub API
export class GitHubBackend implements Backend {
  private readonly baseUrl = "https://api.github.com";
  private readonly rawBaseUrl = "https://raw.githubusercontent.com";
  private readonly token?: string;

  constructor(token?: string) {
    this.token = token;
  }

  private headers(): HeadersInit {
    const h: Record<string, string> = {
      Accept: "application/vnd.github+json",
      "User-Agent": "revanced-api",
    };
    if (this.token) {
      h["Authorization"] = `Bearer ${this.token}`;
    }
    return h;
  }

  private async fetchJson<T>(url: string): Promise<T> {
    const res = await fetch(url, { headers: this.headers() });
    if (!res.ok) {
      throw new Error(`GitHub API error: ${res.status} ${res.statusText} — ${url}`);
    }
    return res.json() as Promise<T>;
  }

  async release(owner: string, repo: string, prerelease: boolean): Promise<BackendRelease> {
    let release: GitHubRelease;

    if (prerelease) {
      const releases = await this.fetchJson<GitHubRelease[]>(
        `${this.baseUrl}/repos/${owner}/${repo}/releases?per_page=1`,
      );
      if (releases.length === 0) {
        throw new Error(`No releases found for ${owner}/${repo}`);
      }
      release = releases[0];
    } else {
      release = await this.fetchJson<GitHubRelease>(
        `${this.baseUrl}/repos/${owner}/${repo}/releases/latest`,
      );
    }

    return {
      tag: release.tag_name,
      releaseNote: release.body ?? "",
      createdAt: formatDatetime(release.created_at),
      prerelease: release.prerelease,
      assets: release.assets.map(
        (a): BackendAsset => ({
          name: a.name,
          downloadUrl: a.browser_download_url,
        }),
      ),
    };
  }

  async fileContent(owner: string, repo: string, branch: string, path: string): Promise<string> {
    const url = `${this.rawBaseUrl}/${owner}/${repo}/${branch}/${path}`;
    const res = await fetch(url, { headers: this.headers() });
    if (!res.ok) {
      throw new Error(`GitHub raw file error: ${res.status} ${res.statusText} — ${url}`);
    }
    return res.text();
  }

  async contributors(owner: string, repo: string): Promise<BackendContributor[]> {
    const contributors = await this.fetchJson<GitHubContributor[]>(
      `${this.baseUrl}/repos/${owner}/${repo}/contributors?per_page=100`,
    );

    return contributors.map((c) => ({
      name: c.login,
      avatarUrl: c.avatar_url,
      url: c.html_url,
      contributions: c.contributions,
    }));
  }

  async members(org: string): Promise<BackendMember[]> {
    const publicMembers = await this.fetchJson<GitHubMember[]>(
      `${this.baseUrl}/orgs/${org}/public_members`,
    );

    const members = await Promise.all(
      publicMembers.map(async (member) => {
        const [user, gpgKeys] = await Promise.all([
          this.fetchJson<GitHubUser>(`${this.baseUrl}/users/${member.login}`),
          this.fetchJson<GitHubGpgKey[]>(`${this.baseUrl}/users/${member.login}/gpg_keys`),
        ]);

        return {
          name: user.login,
          avatarUrl: user.avatar_url,
          url: user.html_url,
          bio: user.bio,
          gpgKeys: {
            ids: gpgKeys.map((k) => k.key_id),
            url: `https://github.com/${user.login}.gpg`,
          },
        } satisfies BackendMember;
      }),
    );

    return members;
  }
}
