import { getBackend, getConfig } from '../config';
import type { Env } from '../types';

export async function getRelease(env: Env, prerelease: boolean) {
    const backend = getBackend(env);
    const { organization, patches } = getConfig(env);

    const release = await backend.release(
        organization,
        patches.repo,
        prerelease
    );
    const asset = release.assets.find((item) =>
        patches.assetRegex.test(item.name)
    );
    const signatureAsset = release.assets.find((item) =>
        patches.signatureAssetRegex.test(item.name)
    );

    return {
        version: release.tag,
        created_at: release.createdAt,
        description: release.releaseNote,
        download_url: asset?.downloadUrl ?? '',
        signature_download_url: signatureAsset?.downloadUrl ?? null
    };
}

export async function getVersion(env: Env, prerelease: boolean) {
    const backend = getBackend(env);
    const { organization, patches } = getConfig(env);

    const release = await backend.release(
        organization,
        patches.repo,
        prerelease
    );
    return { version: release.tag };
}

export async function getHistory(env: Env, prerelease: boolean) {
    const backend = getBackend(env);
    const { organization, patches } = getConfig(env);

    const allReleases = await backend.releases(organization, patches.repo, 100);
    const filtered = prerelease
        ? allReleases
        : allReleases.filter((r) => !r.prerelease);

    return filtered.map((r) => ({
        version: r.tag,
        created_at: r.createdAt,
        description: r.releaseNote
    }));
}

let _publicKeyCache: string | undefined;

export async function getPublicKey(env: Env) {
    if (!_publicKeyCache) {
        const { patches } = getConfig(env);
        const res = await env.ASSETS.fetch(
            new URL(patches.publicKeyFile, 'https://assets.local')
        );
        if (!res.ok) {
            throw new Error(
                `Failed to load public key from ${patches.publicKeyFile}: ${res.status}`
            );
        }
        _publicKeyCache = await res.text();
    }
    return { patches_public_key: _publicKeyCache };
}
