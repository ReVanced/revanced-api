import { drizzle } from "drizzle-orm/d1";
import * as schema from "./schema";
import type { Database } from "../types";

// Creates a Drizzle ORM client from a D1 binding
export function createDb(d1: D1Database): Database {
  return drizzle(d1, { schema });
}
