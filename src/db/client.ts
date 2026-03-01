import { drizzle, DrizzleD1Database } from "drizzle-orm/d1";
import * as schema from "./schema";

export type Database = DrizzleD1Database<typeof schema>;

/* creates a drizzle orm client from a d1 binding -- swap this factory if you want a different db provider (postgres turso etc) */
export function createDb(d1: D1Database): Database {
  return drizzle(d1, { schema });
}
