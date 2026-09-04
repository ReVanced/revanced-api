import { describe, expect, test } from 'bun:test';
import { buildAnnouncementUpdates } from './announcements';
import { UpdateAnnouncementBodySchema } from '../schemas/announcements';

describe('buildAnnouncementUpdates', () => {
    describe('archived_at', () => {
        test('a timestamp is written', () => {
            const updates = buildAnnouncementUpdates({
                archived_at: '2026-01-02T03:04:05.000Z'
            });
            expect(updates.archivedAt).toBe('2026-01-02T03:04:05.000Z');
        });

        test('null clears the value', () => {
            const updates = buildAnnouncementUpdates({ archived_at: null });
            expect('archivedAt' in updates).toBe(true);
            expect(updates.archivedAt).toBeNull();
        });

        test('omitting it leaves the stored value untouched', () => {
            const updates = buildAnnouncementUpdates({ title: 'Unrelated' });
            expect('archivedAt' in updates).toBe(false);
        });
    });

    describe('level', () => {
        test('zero is written', () => {
            const updates = buildAnnouncementUpdates({ level: 0 });
            expect(updates.level).toBe(0);
        });

        test('a non-zero level is written', () => {
            const updates = buildAnnouncementUpdates({ level: 2 });
            expect(updates.level).toBe(2);
        });

        test('omitting it leaves the stored value untouched', () => {
            const updates = buildAnnouncementUpdates({ title: 'Unrelated' });
            expect('level' in updates).toBe(false);
        });
    });

    describe('content', () => {
        test('an empty string clears the content', () => {
            const updates = buildAnnouncementUpdates({ content: '' });
            expect('content' in updates).toBe(true);
            expect(updates.content).toBe('');
        });

        test('omitting it leaves the stored content untouched', () => {
            const updates = buildAnnouncementUpdates({ title: 'Unrelated' });
            expect('content' in updates).toBe(false);
        });

        test('normal content is written', () => {
            const updates = buildAnnouncementUpdates({ content: '<b>hi</b>' });
            expect(updates.content).toBe('<b>hi</b>');
        });
    });

    describe('columns that are NOT NULL in the schema', () => {
        test('created_at null is ignored rather than written', () => {
            const updates = buildAnnouncementUpdates({ created_at: null });
            expect('createdAt' in updates).toBe(false);
        });

        test('created_at with a value is written', () => {
            const updates = buildAnnouncementUpdates({
                created_at: '2026-01-02T03:04:05.000Z'
            });
            expect(updates.createdAt).toBe('2026-01-02T03:04:05.000Z');
        });

        test('title null is ignored rather than written', () => {
            const updates = buildAnnouncementUpdates({ title: null });
            expect('title' in updates).toBe(false);
        });

        test('level null is ignored rather than written', () => {
            const updates = buildAnnouncementUpdates({ level: null });
            expect('level' in updates).toBe(false);
        });
    });

    test('author accepts null because the column is nullable', () => {
        const updates = buildAnnouncementUpdates({ author: null });
        expect('author' in updates).toBe(true);
        expect(updates.author).toBeNull();
    });

    test('an empty body produces no updates at all', () => {
        expect(Object.keys(buildAnnouncementUpdates({}))).toHaveLength(0);
    });
});

describe('UpdateAnnouncementBodySchema round trip', () => {
    test('an omitted level stays undefined and is not written', () => {
        const body = UpdateAnnouncementBodySchema.parse({
            title: 'Title',
            tags: []
        });
        expect(body.level).toBeUndefined();
        expect('level' in buildAnnouncementUpdates(body)).toBe(false);
    });

    test('an explicit level of 0 survives validation and is written', () => {
        const body = UpdateAnnouncementBodySchema.parse({
            title: 'Title',
            tags: [],
            level: 0
        });
        expect(body.level).toBe(0);
        expect(buildAnnouncementUpdates(body).level).toBe(0);
    });

    test('an explicit archived_at of null survives validation and clears', () => {
        const body = UpdateAnnouncementBodySchema.parse({
            title: 'Title',
            tags: [],
            archived_at: null
        });
        expect(body.archived_at).toBeNull();
        expect(buildAnnouncementUpdates(body).archivedAt).toBeNull();
    });

    test('the un-archive payload the website sends actually clears the value', () => {
        const body = UpdateAnnouncementBodySchema.parse({
            title: 'Some announcement',
            content: 'Body',
            tags: ['Important'],
            created_at: '2026-01-02T03:04:05.000Z',
            archived_at: null
        });
        const updates = buildAnnouncementUpdates(body);
        expect(updates.archivedAt).toBeNull();
        expect(updates.createdAt).toBe('2026-01-02T03:04:05.000Z');
    });
});

describe('UpdateAnnouncementBodySchema tags', () => {
    test('a PATCH that does not mention tags is accepted and leaves them alone', () => {
        const body = UpdateAnnouncementBodySchema.parse({ title: 'Title' });
        expect(body.tags).toBeUndefined();
    });

    test('an empty tags array is accepted and means "clear every tag"', () => {
        const body = UpdateAnnouncementBodySchema.parse({ title: 'Title', tags: [] });
        expect(body.tags).toEqual([]);
    });

    test('multiple tags are accepted', () => {
        const body = UpdateAnnouncementBodySchema.parse({
            title: 'Title',
            tags: ['Alpha', 'Beta']
        });
        expect(body.tags).toEqual(['Alpha', 'Beta']);
    });

    test('the website payload with an empty tag selection validates', () => {
        const body = UpdateAnnouncementBodySchema.parse({
            title: 'Some announcement',
            content: 'Body',
            author: '',
            tags: [],
            created_at: '2026-01-02T03:04Z',
            archived_at: null
        });
        expect(body.tags).toEqual([]);
        expect(body.author).toBe('');
        expect(body.archived_at).toBeNull();
    });
});
