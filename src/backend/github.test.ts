import { describe, expect, test } from 'bun:test';
import { formatDatetime } from './github';
import { ReleaseResponseSchema } from '../schemas/releases';
import { z } from '@hono/zod-openapi';

const datetime = z.iso.datetime();

describe('formatDatetime', () => {
    test('keeps a GitHub timestamp valid against the published schema', () => {
        const formatted = formatDatetime('2026-04-26T13:06:56Z');
        expect(datetime.safeParse(formatted).success).toBe(true);
        expect(formatted).toBe('2026-04-26T13:06:56.000Z');
    });

    test('normalises an offset timestamp to UTC', () => {
        const formatted = formatDatetime('2026-04-26T15:06:56+02:00');
        expect(formatted).toBe('2026-04-26T13:06:56.000Z');
        expect(datetime.safeParse(formatted).success).toBe(true);
    });

    test('keeps a millisecond timestamp valid', () => {
        expect(datetime.safeParse(formatDatetime('2026-04-26T13:06:56.123Z')).success).toBe(true);
    });

    test('leaves an unparseable value untouched instead of throwing', () => {
        expect(formatDatetime('not a date')).toBe('not a date');
    });

    test('the value it produces satisfies the release response schema', () => {
        const release = {
            version: 'v2.6.0',
            created_at: formatDatetime('2026-04-26T13:06:56Z'),
            description: 'notes',
            download_url: 'https://example.test/app.apk'
        };
        expect(ReleaseResponseSchema.safeParse(release).success).toBe(true);
    });
});
