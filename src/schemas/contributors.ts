import { z } from '@hono/zod-openapi';

export const ContributorSchema = z.object({
    name: z.string().openapi({ example: 'oSumAtrIX' }),
    avatar_url: z
        .url()
        .openapi({ example: 'https://avatars.githubusercontent.com/u/...' }),
    url: z.url().openapi({ example: 'https://github.com/oSumAtrIX' }),
    contributions: z.number().int().openapi({ example: 542 })
});

export const ContributableSchema = z
    .object({
        name: z.string().openapi({ example: 'ReVanced Patches' }),
        url: z
            .url()
            .openapi({
                example: 'https://github.com/revanced/revanced-patches'
            }),
        contributors: z.array(ContributorSchema)
    })
    .openapi('Contributable');

export const ContributorsResponseSchema = z.array(ContributableSchema);

export const GpgKeySchema = z
    .object({
        id: z.string().openapi({ example: 'ABC123DEF456' }),
        url: z.url().openapi({ example: 'https://github.com/oSumAtrIX.gpg' })
    })
    .nullable();

export const TeamMemberSchema = z
    .object({
        name: z.string().openapi({ example: 'oSumAtrIX' }),
        avatar_url: z
            .url()
            .openapi({
                example: 'https://avatars.githubusercontent.com/u/...'
            }),
        url: z.url().openapi({ example: 'https://github.com/oSumAtrIX' }),
        bio: z.string().nullable().openapi({ example: 'Some bio text' }),
        gpg_key: GpgKeySchema
    })
    .openapi('TeamMember');

export const TeamResponseSchema = z.array(TeamMemberSchema);
