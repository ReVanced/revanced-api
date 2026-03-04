import { OpenAPIHono, createRoute, z } from "@hono/zod-openapi";
import type { Env } from "../types";
import * as patchesService from "../services/patches";

const app = new OpenAPIHono<{ Bindings: Env }>();

app.openapi(
  createRoute({
    method: "get",
    path: "/",
    tags: ["API"],
    summary: "Get public key",
    description: "Get the public key for verifying patches assets.",
    responses: {
      200: {
        content: {
          "text/plain": {
            schema: z.string().openapi({
              example:
                "-----BEGIN PGP PUBLIC KEY BLOCK-----\n...\n-----END PGP PUBLIC KEY BLOCK-----",
            }),
          },
        },
        description: "The public key.",
      },
    },
  }),
  async (c) => {
    const content = await patchesService.getRawPublicKey(c.env);
    return c.text(content);
  },
);

export default app;
