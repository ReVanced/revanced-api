import { drizzle } from "drizzle-orm/d1";
import * as schema from "./schema";
import type { Database } from "../types";

export function createDb(d1: D1Database): Database {
  return drizzle(d1, { schema });
}

let _database: Database | undefined;

export function getDatabase(d1: D1Database): Database {
  return (_database ??= createDb(d1));
}
