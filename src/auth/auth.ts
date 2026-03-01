import type { MiddlewareHandler } from "hono";
import type { Env } from "../types";

/* bearer token auth middleware -- checks the Authorization header against API_TOKEN env var, replace this factory if you wanna swap auth (jwt oauth etc) */
export function authMiddleware(): MiddlewareHandler<{ Bindings: Env }> {
  return async (c, next) => {
    const authHeader = c.req.header("Authorization");

    if (!authHeader) {
      return c.json({ error: "Missing Authorization header" }, 401);
    }

    const [scheme, token] = authHeader.split(" ", 2);

    if (scheme !== "Bearer" || !token) {
      return c.json({ error: "Invalid Authorization header format. Expected: Bearer <token>" }, 401);
    }

    if (token !== c.env.API_TOKEN) {
      return c.json({ error: "Invalid token" }, 403);
    }

    await next();
  };
}
