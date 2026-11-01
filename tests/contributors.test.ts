import { afterEach, test } from 'node:test';
import assert from 'node:assert/strict';
import contributorsApp from '../src/routes/contributors';
import { ContributorsResponseSchema } from '../src/schemas/contributors';
import type { Env } from '../src/types';

const originalFetch = globalThis.fetch;
const env = {
    ORGANIZATION: 'ReVanced',
    PATCHES_ASSET_REGEX: 'rvp$',
    PATCHES_SIGNATURE_ASSET_REGEX: 'asc$',
    MANAGER_ASSET_REGEX: 'apk$',
    MANAGER_DOWNLOADERS_ASSET_REGEX: 'apk$',
    CONTRIBUTORS_REPOS:
        ' , REVANCED-API : ReVanced API , unnamed: , missing:Missing, ignored:Ignored, colon:Name: With colon, ',
    IGNORED_CONTRIBUTOR_REPOS: ' IGNORED , , ',
    IGNORED_CONTRIBUTORS: 'bot',
    API_VERSION: '5'
} as Env;

afterEach(() => {
    globalThis.fetch = originalFetch;
});

test('contributors endpoint discovers new repositories, applies overrides and exclusions, and preserves its response schema', async () => {
    const requestedRepositories: string[] = [];
    globalThis.fetch = async (input) => {
        const url = new URL(String(input));
        if (url.pathname === '/orgs/ReVanced/repos') {
            return Response.json(
                [
                    'colon',
                    'empty',
                    'Ignored',
                    'new-project',
                    'revanced-api',
                    'unnamed'
                ].map((name) => ({ name, private: false }))
            );
        }
        const match = url.pathname.match(
            /^\/repos\/ReVanced\/([^/]+)\/contributors$/
        );
        assert.ok(match, `Unexpected request: ${url}`);
        requestedRepositories.push(match[1]);
        if (match[1] === 'empty') return new Response(null, { status: 204 });
        return Response.json([
            {
                login: 'alice',
                avatar_url: 'https://example.com/avatar',
                html_url: 'https://github.com/alice',
                contributions: 2
            },
            {
                login: 'bot',
                avatar_url: 'https://example.com/bot',
                html_url: 'https://github.com/bot',
                contributions: 3
            }
        ]);
    };

    const response = await contributorsApp.request('/', {}, env);
    assert.equal(response.status, 200);
    assert.equal(
        response.headers.get('Cache-Control'),
        'public, max-age=86400'
    );
    const result = ContributorsResponseSchema.parse(await response.json());
    assert.deepEqual(
        result.map(({ name }) => name),
        ['Name: With colon', 'empty', 'new-project', 'ReVanced API', 'unnamed']
    );
    assert.deepEqual(requestedRepositories, [
        'colon',
        'empty',
        'new-project',
        'revanced-api',
        'unnamed'
    ]);
    assert.deepEqual(result[1].contributors, []);
    assert.deepEqual(result[3], {
        name: 'ReVanced API',
        url: 'https://github.com/ReVanced/revanced-api',
        contributors: [
            {
                name: 'alice',
                avatar_url: 'https://example.com/avatar',
                url: 'https://github.com/alice',
                contributions: 2
            }
        ]
    });
});

test('contributors endpoint handles an organization without public repositories', async () => {
    let requests = 0;
    globalThis.fetch = async () => {
        requests++;
        return Response.json([]);
    };
    const response = await contributorsApp.request('/', {}, env);
    assert.equal(response.status, 200);
    assert.deepEqual(await response.json(), []);
    assert.equal(requests, 1);
});
