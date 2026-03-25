import type { Env } from './types';
import type { Backend } from './backend/types';
import { GitHubBackend } from './backend/github';
import { GiteaBackend } from './backend/gitea';
import { GitLabBackend } from './backend/gitlab';

export interface Config {
    organization: string;
    patches: {
        repo: string;
        assetRegex: RegExp;
        signatureAssetRegex: RegExp;
        publicKeyFile: string;
    };
    manager: {
        repo: string;
        assetRegex: RegExp;
        downloadersRepo: string;
        downloadersAssetRegex: RegExp;
    };
    contributorRepos: { repo: string; name: string }[];
    apiVersion: string;
}

let _config: Config | undefined;

export function getConfig(env: Env): Config {
    return (_config ??= {
        organization: env.ORGANIZATION,
        patches: {
            repo: env.PATCHES_REPO,
            assetRegex: new RegExp(env.PATCHES_ASSET_REGEX),
            signatureAssetRegex: new RegExp(env.PATCHES_SIGNATURE_ASSET_REGEX),
            publicKeyFile: env.PATCHES_PUBLIC_KEY_FILE
        },
        manager: {
            repo: env.MANAGER_REPO,
            assetRegex: new RegExp(env.MANAGER_ASSET_REGEX),
            downloadersRepo: env.MANAGER_DOWNLOADERS_REPO,
            downloadersAssetRegex: new RegExp(
                env.MANAGER_DOWNLOADERS_ASSET_REGEX
            )
        },
        contributorRepos: env.CONTRIBUTORS_REPOS.split(',').map((entry) => {
            const [repo, ...nameParts] = entry.trim().split(':');
            return { repo: repo.trim(), name: nameParts.join(':').trim() };
        }),
        apiVersion: env.API_VERSION
    });
}

let _backend: Backend | undefined;

export function getBackend(env: Env): Backend {
    return (_backend ??= createBackend(env));
}

function createBackend(env: Env): Backend {
    switch (env.BACKEND) {
        case 'github':
            return new GitHubBackend(env.BACKEND_TOKEN);
        case 'gitea':
            return new GiteaBackend(env.GITEA_URL!, env.BACKEND_TOKEN);
        case 'gitlab':
            return new GitLabBackend(env.GITLAB_URL!, env.BACKEND_TOKEN);
        default:
            throw new Error(`Unknown backend: ${env.BACKEND}`);
    }
}
