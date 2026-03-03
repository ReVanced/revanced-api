import { z } from "@hono/zod-openapi";

export const PrereleaseQuerySchema = z.object({
  prerelease: z
    .enum(["true", "false"])
    .optional()
    .default("false")
    .openapi({ description: "Whether to get prerelease data", example: "false" }),
});

export const ErrorResponseSchema = z.object({
  error: z.string().openapi({ example: "Something went wrong" }),
});
