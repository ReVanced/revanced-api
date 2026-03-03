import { z } from "@hono/zod-openapi";

export const ReleaseResponseSchema = z
  .object({
    version: z.string().openapi({ example: "v4.0.0" }),
    created_at: z.string().openapi({ example: "2025-01-15T10:30:00" }),
    description: z.string().openapi({ example: "Release notes markdown here..." }),
    download_url: z.string().url().openapi({
      example:
        "https://github.com/revanced/revanced-patches/releases/download/v4.0.0/patches-4.0.0.rvp",
    }),
    signature_download_url: z
      .string()
      .url()
      .nullable()
      .optional()
      .openapi({
        example:
          "https://github.com/revanced/revanced-patches/releases/download/v4.0.0/patches-4.0.0.rvp.asc",
      }),
  })
  .openapi("Release");

export const VersionResponseSchema = z
  .object({
    version: z.string().openapi({ example: "v4.0.0" }),
  })
  .openapi("Version");

export const HistoryResponseSchema = z
  .object({
    history: z.string().openapi({ example: "# Changelog\n\n## v4.0.0\n..." }),
  })
  .openapi("History");

export const PublicKeyResponseSchema = z
  .object({
    patches_public_key: z.string().openapi({
      example: "-----BEGIN PGP PUBLIC KEY BLOCK-----\n...\n-----END PGP PUBLIC KEY BLOCK-----",
      description: "The PGP public key for verifying patches assets.",
    }),
  })
  .openapi("PublicKey");
