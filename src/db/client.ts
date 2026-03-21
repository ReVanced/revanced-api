import { drizzle } from 'drizzle-orm/d1';
import * as schema from './schema';
import type { Database } from '../types';

let _database: Database | undefined;

export function getDatabase(d1: D1Database): Database {
    return (_database ??= drizzle(d1, { schema }));
}
