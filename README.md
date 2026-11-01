<p align="center">
  <picture>
    <source
      width="256px"
      media="(prefers-color-scheme: dark)"
      srcset="assets/revanced-headline/revanced-headline-vertical-dark.svg"
    >
    <img 
      width="256px"
      src="assets/revanced-headline/revanced-headline-vertical-light.svg"
    >
  </picture>
  <br>
  <a href="https://revanced.app/">
     <picture>
         <source height="24px" media="(prefers-color-scheme: dark)" srcset="assets/revanced-logo/revanced-logo.svg" />
         <img height="24px" src="assets/revanced-logo/revanced-logo.svg" />
     </picture>
   </a>&nbsp;&nbsp;&nbsp;
   <a href="https://github.com/ReVanced">
       <picture>
           <source height="24px" media="(prefers-color-scheme: dark)" srcset="https://i.ibb.co/dMMmCrW/Git-Hub-Mark.png" />
           <img height="24px" src="https://i.ibb.co/9wV3HGF/Git-Hub-Mark-Light.png" />
       </picture>
   </a>&nbsp;&nbsp;&nbsp;
   <a href="http://revanced.app/discord">
       <picture>
           <source height="24px" media="(prefers-color-scheme: dark)" srcset="https://user-images.githubusercontent.com/13122796/178032563-d4e084b7-244e-4358-af50-26bde6dd4996.png" />
           <img height="24px" src="https://user-images.githubusercontent.com/13122796/178032563-d4e084b7-244e-4358-af50-26bde6dd4996.png" />
       </picture>
   </a>&nbsp;&nbsp;&nbsp;
   <a href="https://reddit.com/r/revancedapp">
       <picture>
           <source height="24px" media="(prefers-color-scheme: dark)" srcset="https://user-images.githubusercontent.com/13122796/178032351-9d9d5619-8ef7-470a-9eec-2744ece54553.png" />
           <img height="24px" src="https://user-images.githubusercontent.com/13122796/178032351-9d9d5619-8ef7-470a-9eec-2744ece54553.png" />
       </picture>
   </a>&nbsp;&nbsp;&nbsp;
   <a href="https://t.me/app_revanced">
      <picture>
         <source height="24px" media="(prefers-color-scheme: dark)" srcset="https://user-images.githubusercontent.com/13122796/178032213-faf25ab8-0bc3-4a94-a730-b524c96df124.png" />
         <img height="24px" src="https://user-images.githubusercontent.com/13122796/178032213-faf25ab8-0bc3-4a94-a730-b524c96df124.png" />
      </picture>
   </a>&nbsp;&nbsp;&nbsp;
   <a href="https://x.com/revancedapp">
      <picture>
         <source media="(prefers-color-scheme: dark)" srcset="https://user-images.githubusercontent.com/93124920/270180600-7c1b38bf-889b-4d68-bd5e-b9d86f91421a.png">
         <img height="24px" src="https://user-images.githubusercontent.com/93124920/270108715-d80743fa-b330-4809-b1e6-79fbdc60d09c.png" />
      </picture>
   </a>&nbsp;&nbsp;&nbsp;
   <a href="https://www.youtube.com/@ReVanced">
      <picture>
         <source height="24px" media="(prefers-color-scheme: dark)" srcset="https://user-images.githubusercontent.com/13122796/178032714-c51c7492-0666-44ac-99c2-f003a695ab50.png" />
         <img height="24px" src="https://user-images.githubusercontent.com/13122796/178032714-c51c7492-0666-44ac-99c2-f003a695ab50.png" />
     </picture>
   </a>
   <br>
   <br>
   Continuing the legacy of Vanced
</p>

# 🚀 ReVanced API

![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/ReVanced/revanced-api/deploy.yml)
![AGPLv3 License](https://img.shields.io/badge/License-AGPL%20v3-yellow.svg)

API server for ReVanced.

## ❓ About

ReVanced API is a server that is used as the backend for ReVanced.
ReVanced API acts as the data source for [ReVanced Website](https://github.com/ReVanced/revanced-website) and
powers [ReVanced Manager](https://github.com/ReVanced/revanced-manager)
with updates and ReVanced Patches.

## 💪 Features

Some of the features ReVanced API include:

- 📢 **Announcements**: Post and get announcements
- ℹ️ **About**: Get more information such as a description, ways to donate to,
  and links of the hoster of ReVanced API
- 💊 **Manager**: Get the latest updates of ReVanced Manager and its downloaders
- 🧩 **Patches**: Get the latest updates of ReVanced Patches, directly from ReVanced API
- 👥 **Contributors**: List all contributors involved in the project

## 🚀 How to get started

ReVanced API is deployed as a Cloudflare Worker.

### 🧑‍💻 Local development

1. Install dependencies with `bun install`
2. Copy `.env.example` to `.env` and fill in the required environment variables
3. Copy `about.example.json` to `about.json` and customize the API information
4. Create a local D1 database and run migrations with `bun run db:migration:apply`
5. Start the development server with `bun run dev`

After setup, run `bun run test` for the tests and `bun run typecheck` to check
the application and test types.

### 👥 Contributor repositories

The contributors endpoint automatically discovers all public repositories in
`ORGANIZATION`, including forks and archived repositories. New public repositories
are included without updating configuration. Repositories are listed by name in
ascending order.

- `CONTRIBUTORS_REPOS` is an optional comma-separated list of
  `repository:Display name` overrides. Repositories without an override (or with
  an empty display name) use their repository name. Overrides do not add
  repositories that are absent from discovery.
- `IGNORED_CONTRIBUTOR_REPOS` is an optional comma-separated list of repository
  names to exclude. Exclusions take precedence over display-name overrides.
- Both settings trim whitespace and match repository names case-insensitively.
  `IGNORED_CONTRIBUTORS` continues to filter individual contributors.

For example, `CONTRIBUTORS_REPOS=revanced-api:ReVanced API` and
`IGNORED_CONTRIBUTOR_REPOS=old-project,example-project` customize the API's name
and omit those two repositories while including newly discovered repositories.
Empty repositories have an empty contributors list.

When migrating, `CONTRIBUTORS_REPOS` supplies display names rather than an
allowlist. Add any public repositories that should stay hidden to
`IGNORED_CONTRIBUTOR_REPOS`. Clients may retain an earlier response for the
contributors endpoint's one-day cache duration.

### 🌐 Deployment

```bash
bun run db:migration:apply --remote # Configure the database_id in wrangler.toml after this command.
bun run deploy
```

## 📚 Everything else

### 📙 Contributing

Thank you for considering contributing to ReVanced API. You can find the contribution
guidelines [here](CONTRIBUTING.md).

## 📜 Licence

ReVanced API is licensed under the AGPLv3 licence. Please see the [licence file](LICENSE) for more information.
[tl;dr](https://www.tldrlegal.com/license/gnu-affero-general-public-license-v3-agpl-3-0) you may copy, distribute and
modify ReVanced API as long as you track changes/dates in source files.
Any modifications to ReVanced API must also be made available under the GPL along with build & install instructions.
