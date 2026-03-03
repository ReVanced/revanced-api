import type { Env } from "./types";
import { GitHubBackend } from "./backend/github";

export interface Config {
  organization: string;
  patches: {
    repo: string;
    assetRegex: RegExp;
    signatureAssetRegex: RegExp;
    historyFile: string;
    publicKeyFile: string;
  };
  manager: {
    repo: string;
    assetRegex: RegExp;
    downloadersRepo: string;
    downloadersAssetRegex: RegExp;
    historyFile: string;
  };
  branches: {
    main: string;
    prerelease: string;
  };
  contributorRepos: { repo: string; name: string }[];
  apiVersion: string;
}

function parseConfig(env: Env): Config {
  return {
    organization: env.ORGANIZATION,
    patches: {
      repo: env.PATCHES_REPO,
      assetRegex: new RegExp(env.PATCHES_ASSET_REGEX),
      signatureAssetRegex: new RegExp(env.PATCHES_SIGNATURE_ASSET_REGEX),
      historyFile: env.PATCHES_HISTORY_FILE,
      publicKeyFile: env.PATCHES_PUBLIC_KEY_FILE,
    },
    manager: {
      repo: env.MANAGER_REPO,
      assetRegex: new RegExp(env.MANAGER_ASSET_REGEX),
      downloadersRepo: env.MANAGER_DOWNLOADERS_REPO,
      downloadersAssetRegex: new RegExp(env.MANAGER_DOWNLOADERS_ASSET_REGEX),
      historyFile: env.MANAGER_HISTORY_FILE,
    },
    branches: {
      main: env.MAIN_BRANCH,
      prerelease: env.PRERELEASE_BRANCH,
    },
    contributorRepos: env.CONTRIBUTORS_REPOS.split(",").map((entry) => {
      const [repo, ...nameParts] = entry.trim().split(":");
      return { repo: repo.trim(), name: nameParts.join(":").trim() };
    }),
    apiVersion: env.API_VERSION,
  };
}

let _config: Config | undefined;
let _backend: GitHubBackend | undefined;

export function getConfig(env: Env): Config {
  return (_config ??= parseConfig(env));
}

export function getBackend(env: Env): GitHubBackend {
  return (_backend ??= new GitHubBackend(env.GITHUB_TOKEN));
}
