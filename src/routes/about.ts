import { OpenAPIHono, createRoute, z } from "@hono/zod-openapi";
import type { Env, AppVariables } from "../types";
import aboutData from "../data/about.json";

const app = new OpenAPIHono<{ Bindings: Env; Variables: AppVariables }>();

// Define the about schema to match about.example.json
const AboutResponse = z
  .object({
    name: z.string(),
    about: z.string(),
    keys: z.string(),
    branding: z
      .object({
        logo: z.string().url(),
      })
      .optional()
      .nullable(),
    status: z.string(),
    contact: z
      .object({
        email: z.string().email(),
      })
      .optional()
      .nullable(),
    socials: z
      .array(
        z.object({
          name: z.string(),
          url: z.string().url(),
          preferred: z.boolean().optional(),
        }),
      )
      .optional()
      .nullable(),
    donations: z
      .object({
        wallets: z
          .array(
            z.object({
              network: z.string(),
              currency_code: z.string(),
              address: z.string(),
              preferred: z.boolean().optional(),
            }),
          )
          .optional()
          .nullable(),
        links: z
          .array(
            z.object({
              name: z.string(),
              url: z.string().url(),
              preferred: z.boolean().optional(),
            }),
          )
          .optional()
          .nullable(),
      })
      .optional()
      .nullable(),
  })
  .openapi("About");

// GET /about

const getAboutRoute = createRoute({
  method: "get",
  path: "/",
  tags: ["API"],
  summary: "Get about",
  description: "Get information about the API",
  responses: {
    200: {
      content: { "application/json": { schema: AboutResponse } },
      description: "Information about the API",
    },
  },
});

app.openapi(getAboutRoute, (c) => {
  return c.json(aboutData as z.infer<typeof AboutResponse>, 200);
});

export default app;
