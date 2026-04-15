import { getBackend, getConfig } from '../config';
import type { Env } from '../types';

export async function getRelease(env: Env, prerelease: boolean) {
    const backend = getBackend(env);
    const { organization, manager } = getConfig(env);

    const release = await backend.release(
        organization,
        manager.repo,
        prerelease
    );
    const asset = release.assets.find((item) =>
        manager.assetRegex.test(item.name)
    );

    return {
        version: release.tag,
        created_at: release.createdAt,
        description: release.releaseNote,
        download_url: asset?.downloadUrl ?? ''
    };
}

export async function getVersion(env: Env, prerelease: boolean) {
    const backend = getBackend(env);
    const { organization, manager } = getConfig(env);

    const release = await backend.release(
        organization,
        manager.repo,
        prerelease
    );
    return { version: release.tag };
}

export async function getHistory(env: Env, prerelease: boolean) {
    const backend = getBackend(env);
    const { organization, manager } = getConfig(env);

    const allReleases = await backend.releases(organization, manager.repo, 100);
    const filtered = prerelease
        ? allReleases
        : allReleases.filter((r) => !r.prerelease);

    return filtered.map((r) => ({
        version: r.tag,
        created_at: r.createdAt,
        description: r.releaseNote
    }));
}

export async function getDownloadersRelease(env: Env, prerelease: boolean) {
    const backend = getBackend(env);
    const { organization, manager } = getConfig(env);

    const release = await backend.release(
        organization,
        manager.downloadersRepo,
        prerelease
    );
    const asset = release.assets.find((item) =>
        manager.downloadersAssetRegex.test(item.name)
    );

    return {
        version: release.tag,
        created_at: release.createdAt,
        description: release.releaseNote,
        download_url: asset?.downloadUrl ?? ''
    };
}

export async function getDownloadersVersion(env: Env, prerelease: boolean) {
    const backend = getBackend(env);
    const { organization, manager } = getConfig(env);

    const release = await backend.release(
        organization,
        manager.downloadersRepo,
        prerelease
    );
    return { version: release.tag };
}
