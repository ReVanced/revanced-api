import { z } from "@hono/zod-openapi";

export const PrereleaseQuery = z.object({
  prerelease: z
    .enum(["true", "false"])
    .optional()
    .default("false")
    .openapi({ description: "Whether to get prerelease data", example: "false" }),
});

export const ErrorResponse = z.object({
  error: z.string().openapi({ example: "Something went wrong" }),
});

export const AnnouncementIdParam = z.object({
  id: z.coerce
    .number()
    .int()
    .openapi({ description: "Announcement ID", example: 1, param: { in: "path" } }),
});
