import { afterEach, test } from 'node:test';
import assert from 'node:assert/strict';
import { GitHubBackend } from '../src/backend/github';

const originalFetch = globalThis.fetch;

afterEach(() => {
    globalThis.fetch = originalFetch;
});

test('discovers all pages of public repositories with stable ordering', async () => {
    const requests: string[] = [];
    const firstPage = Array.from({ length: 100 }, (_, i) => ({
        name: `project-${i}`,
        private: false
    }));
    globalThis.fetch = async (input, init) => {
        const url = new URL(String(input));
        requests.push(url.href);
        assert.equal(url.pathname, '/orgs/ReVanced/repos');
        assert.equal(url.searchParams.get('type'), 'public');
        assert.equal(url.searchParams.get('sort'), 'full_name');
        assert.equal(url.searchParams.get('direction'), 'asc');
        assert.equal(url.searchParams.get('per_page'), '100');
        assert.equal(
            new Headers(init?.headers).get('Authorization'),
            'Bearer test-token'
        );
        return Response.json(
            url.searchParams.get('page') === '1'
                ? firstPage
                : [{ name: 'project-new', private: false }]
        );
    };

    assert.deepEqual(
        await new GitHubBackend('test-token').repositories('ReVanced'),
        [...firstPage.map((repo) => repo.name), 'project-new']
    );
    assert.deepEqual(
        requests.map((url) => new URL(url).searchParams.get('page')),
        ['1', '2']
    );
});

test('uses the unfiltered page size to decide whether to fetch another page', async () => {
    let requests = 0;
    globalThis.fetch = async () => {
        requests++;
        return Response.json(
            requests === 1
                ? Array.from({ length: 100 }, () => ({
                      name: 'private-repo',
                      private: true
                  }))
                : [{ name: 'public-repo', private: false }]
        );
    };

    assert.deepEqual(await new GitHubBackend().repositories('ReVanced'), [
        'public-repo'
    ]);
    assert.equal(requests, 2);
});

test('returns an empty list for an organization with no public repositories', async () => {
    globalThis.fetch = async (_input, init) => {
        assert.equal(new Headers(init?.headers).has('Authorization'), false);
        return Response.json([]);
    };
    assert.deepEqual(await new GitHubBackend().repositories('ReVanced'), []);
});

test('checks the next page after an exactly full final page', async () => {
    let requests = 0;
    globalThis.fetch = async () =>
        Response.json(
            ++requests === 1
                ? Array.from({ length: 100 }, (_, i) => ({
                      name: `repo-${i}`,
                      private: false
                  }))
                : []
        );

    assert.equal(
        (await new GitHubBackend().repositories('ReVanced')).length,
        100
    );
    assert.equal(requests, 2);
});

test('propagates GitHub errors instead of returning a partial repository list', async () => {
    let requests = 0;
    globalThis.fetch = async () =>
        ++requests === 1
            ? Response.json(
                  Array.from({ length: 100 }, (_, i) => ({
                      name: `repo-${i}`,
                      private: false
                  }))
              )
            : new Response(null, { status: 403, statusText: 'Forbidden' });

    await assert.rejects(
        new GitHubBackend().repositories('ReVanced'),
        /GitHub API error: 403 Forbidden/
    );
});

test('empty repositories have no contributors', async () => {
    globalThis.fetch = async () => new Response(null, { status: 204 });
    assert.deepEqual(
        await new GitHubBackend().contributors('ReVanced', 'empty'),
        []
    );
});

test('contributor errors are not treated as empty repositories', async () => {
    globalThis.fetch = async () =>
        new Response(null, { status: 404, statusText: 'Not Found' });
    await assert.rejects(
        new GitHubBackend().contributors('ReVanced', 'missing'),
        /GitHub API error: 404 Not Found/
    );
});
