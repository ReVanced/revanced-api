import { getBackend, getConfig } from '../config';
import type { Env } from '../types';

export async function getContributors(env: Env) {
    const backend = getBackend(env);
    const {
        organization,
        contributorRepos,
        ignoredContributorRepos,
        ignoredContributors
    } = getConfig(env);
    const repositories = await backend.repositories(organization);
    const names = new Map(
        contributorRepos.map(({ repo, name }) => [repo.toLowerCase(), name])
    );
    const ignoredRepos = new Set(ignoredContributorRepos);

    const results = await Promise.all(
        repositories
            .filter((repo) => !ignoredRepos.has(repo.toLowerCase()))
            .map(async (repo) => {
                const contributors = await backend.contributors(
                    organization,
                    repo
                );
                return {
                    name: names.get(repo.toLowerCase()) || repo,
                    url: backend.repositoryUrl(organization, repo),
                    contributors: contributors
                        .filter(
                            (contributor) =>
                                !ignoredContributors.includes(contributor.name)
                        )
                        .map((contributor) => ({
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
