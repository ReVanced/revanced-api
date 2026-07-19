import { z } from '@hono/zod-openapi';

export const ReleaseResponseSchema = z
    .object({
        version: z.string().openapi({ example: 'v4.0.0' }),
        created_at: z.iso
            .datetime()
            .openapi({ example: '1970-01-01T00:00:00.000Z' }),
        description: z
            .string()
            .openapi({ example: 'Release notes markdown here...' }),
        download_url: z.string().url().openapi({
            example:
                'https://github.com/revanced/revanced-patches/releases/download/v4.0.0/patches-4.0.0.rvp'
        }),
        signature_download_url: z.string().url().nullable().optional().openapi({
            example:
                'https://github.com/revanced/revanced-patches/releases/download/v4.0.0/patches-4.0.0.rvp.asc'
        })
    })
    .openapi('Release');

export const VersionResponseSchema = z
    .object({
        version: z.string().openapi({ example: 'v4.0.0' })
    })
    .openapi('Version');

export const ReleaseSimpleSchema = z
    .object({
        version: z.string().openapi({ example: 'v4.0.0' }),
        created_at: z.iso
            .datetime()
            .openapi({ example: '1970-01-01T00:00:00.000Z' }),
        description: z
            .string()
            .openapi({ example: 'Release notes markdown here...' })
    })
    .openapi('ReleaseSimple');

export const HistoryResponseSchema = z.array(ReleaseSimpleSchema);

export const PublicKeyResponseSchema = z
    .object({
        patches_public_key: z.string().openapi({
            example:
                '-----BEGIN PGP PUBLIC KEY BLOCK-----\n...\n-----END PGP PUBLIC KEY BLOCK-----',
            description: 'The PGP public key for verifying patches assets.'
        })
    })
    .openapi('PublicKey');
