import { getBackend, getConfig } from "../config";
import type { Env } from "../types";
import patchesPublicKey from "../../data/patches-public-key.txt";

export async function getRelease(env: Env, prerelease: boolean) {
  const backend = getBackend(env);
  const { organization, patches } = getConfig(env);

  const release = await backend.release(organization, patches.repo, prerelease);
  const asset = release.assets.find((item) => patches.assetRegex.test(item.name));
  const signatureAsset = release.assets.find((item) => patches.signatureAssetRegex.test(item.name));

  return {
    version: release.tag,
    created_at: release.createdAt,
    description: release.releaseNote,
    download_url: asset?.downloadUrl ?? "",
    signature_download_url: signatureAsset?.downloadUrl ?? null,
  };
}

export async function getVersion(env: Env, prerelease: boolean) {
  const backend = getBackend(env);
  const { organization, patches } = getConfig(env);

  const release = await backend.release(organization, patches.repo, prerelease);
  return { version: release.tag };
}

export async function getHistory(env: Env, prerelease: boolean) {
  const backend = getBackend(env);
  const { organization, patches, branches } = getConfig(env);

  if (!patches.historyFile) {
    return null;
  }

  const branch = prerelease ? branches.prerelease : branches.main;
  const content = await backend.fileContent(organization, patches.repo, branch, patches.historyFile);
  return { history: content };
}

export function getPublicKey() {
  return { patches_public_key: patchesPublicKey };
}
