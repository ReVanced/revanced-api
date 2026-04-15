import { getBackend, getConfig } from '../config';
import type { Env } from '../types';

export async function getContributors(env: Env) {
    const backend = getBackend(env);
    const { organization, contributorRepos } = getConfig(env);

    const results = await Promise.all(
        contributorRepos.map(async ({ repo, name }) => {
            const contributors = await backend.contributors(organization, repo);
            return {
                name,
                url: backend.repositoryUrl(organization, repo),
                contributors: contributors.map((contributor) => ({
                    name: contributor.name,
                    avatar_url: contributor.avatarUrl,
                    url: contributor.url,
                    contributions: contributor.contributions
                }))
            };
        })
    );

    return results;
}
