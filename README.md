# ReVanced API

![GitHub](https://img.shields.io/github/license/ReVanced/revanced-api)

API server for ReVanced, built with [Hono](https://hono.dev) and deployable to Cloudflare Workers.

## Features

- **GitHub API proxy** — Patches, Manager, Contributors, Team endpoints
- **Announcements CRUD** — Backed by Cloudflare D1 via Drizzle ORM
- **Auto-generated OpenAPI spec** — via `@hono/zod-openapi`
- **Swagger UI** — served at `/`
- **Bearer token authentication** — for admin endpoints
- **Serverless** — Cloudflare Workers (primary), adaptable to Node.js, Vercel, etc.

## Quick Start

```bash
# Install dependencies
npm install

# Create local D1 database and run migrations
npm run db:migrate:local

# Start development server
npm run dev
```

Create a `.dev.vars` file for local secrets:

```
API_TOKEN=your-secret-token
GITHUB_TOKEN=ghp_your-github-token
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Swagger UI |
| GET | `/openapi` | OpenAPI JSON spec |
| GET | `/v1/ping` | Health check (204) |
| GET | `/v1/about` | API information |
| GET | `/v1/patches` | Latest patches release |
| GET | `/v1/patches/version` | Latest patches version |
| GET | `/v1/patches/history` | Patches changelog |
| GET | `/v1/manager` | Latest manager release |
| GET | `/v1/manager/version` | Latest manager version |
| GET | `/v1/manager/history` | Manager changelog |
| GET | `/v1/manager/downloaders` | Latest downloaders release |
| GET | `/v1/manager/downloaders/version` | Latest downloaders version |
| GET | `/v1/contributors` | Repository contributors |
| GET | `/v1/team` | Organization team members |
| GET | `/v1/backend/rate_limit` | GitHub API rate limit |
| GET | `/v1/announcements` | All announcements |
| POST | `/v1/announcements` | Create announcement 🔒 |
| PATCH | `/v1/announcements/:id` | Update announcement 🔒 |
| DELETE | `/v1/announcements/:id` | Delete announcement 🔒 |

🔒 = Requires `Authorization: Bearer <token>` header

## Configuration

Environment variables are configured in `wrangler.toml` (non-secrets) and via `wrangler secret put` or `.dev.vars` (secrets).

See `.env.example` for the required secrets.

## Deployment

```bash
# Deploy to Cloudflare Workers
npm run deploy

# Run remote D1 migrations
npm run db:migrate:remote
```

## License

ReVanced API is licensed under the [AGPLv3 License](LICENSE).
