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

![GitHub Workflow Status (with event)](https://img.shields.io/github/actions/workflow/status/ReVanced/revanced-api/release.yml)
![AGPLv3 License](https://img.shields.io/badge/License-AGPL%20v3-yellow.svg)

API server for ReVanced.

## ❓ About

ReVanced API is a server that is used as the backend for ReVanced.
ReVanced API acts as the data source for [ReVanced Website](https://github.com/ReVanced/revanced-website) and powers [ReVanced Manager](https://github.com/ReVanced/revanced-manager)
with updates and ReVanced Patches.

## 💪 Features

Some of the features ReVanced API include:

- 📢 **Announcements**: Post and get announcements grouped by channels
- ℹ️ **About**: Get more information such as a description, ways to donate to, 
and links of the hoster of ReVanced API
- 🧩 **Patches**: Get the latest updates of ReVanced Patches, directly from ReVanced API
- 👥 **Contributors**: List all contributors involved in the project
- 🔄 **Backwards compatibility**: Proxy an old API for migration purposes and backwards compatibility

## 🚀 How to get started

ReVanced API can be deployed as a Docker container or used standalone.

## 🐳 Docker

To deploy ReVanced API as a Docker container, you can use Docker Compose or Docker CLI.  
The Docker image is published on GitHub Container registry,
so before you can pull the image, you need to [authenticate to the Container registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry#authenticating-to-the-container-registry).

### 🗄️ Docker Compose

1. Create an `.env` file using [.env.example](.env.example) as a template
2. Create a `configuration.toml` file using [configuration.example.toml](configuration.example.toml) as a template
3. Create a `docker-compose.yml` file using [docker-compose.example.yml](docker-compose.example.yml) as a template
4. Run `docker-compose up -d` to start the server

### 💻 Docker CLI

1. Create an `.env` file using [.env.example](.env.example) as a template
2. Create a `configuration.toml` file using [configuration.example.toml](configuration.example.toml) as a template
3. Start the container using the following command:
   ```shell
   docker run -d --name revanced-api \
    # Mount the .env file
    -v $(pwd)/.env:/app/.env \
    # Mount the configuration.toml file
    -v $(pwd)/configuration.toml:/app/configuration.toml \
    # Mount the persistence folder
    -v $(pwd)/persistence:/app/persistence \
    # Expose the port 8888
    -p 8888:8888 \
    # Use the start command to start the server
    -e COMMAND=start \
    # Pull the image from the GitHub Container registry
    ghcr.io/revanced/revanced-api:latest
   ```

## 🖥️ Standalone

To deploy ReVanced API standalone, you can either use the pre-built executable or build it from source.

### 📦 Pre-built executable

A Java Runtime Environment (JRE) must be installed.

1. [Download](https://github.com/ReVanced/revanced-api/releases/latest) ReVanced API to a folder
2. In the same folder, create an `.env` file using [.env.example](.env.example) as a template
3. In the same folder, create a `configuration.toml` file
using [configuration.example.toml](configuration.example.toml) as a template
4. Run `java -jar revanced-api.jar start` to start the server

### 🛠️ From source

A Java Development Kit (JDK) and Git must be installed.

1. Run `git clone git@github.com:ReVanced/revanced-api.git` to clone the repository
2. Copy [.env.example](.env.example) to `.env` and fill in the required values
3. Copy [configuration.example.toml](configuration.example.toml) to `configuration.toml` and fill in the required values
4. Run `gradlew run --args=start` to start the server

## 📚 Everything else

### 📙 Contributing

Thank you for considering contributing to ReVanced API. You can find the contribution guidelines [here](CONTRIBUTING.md).

### 🛠️ Building

To build ReVanced API, a Java Development Kit (JDK) and Git must be installed.  
Follow the steps below to build ReVanced API:

1. Run `git clone git@github.com:ReVanced/revanced-api.git` to clone the repository
2. Run `gradlew build` to build the project

## 📜 Licence

ReVanced API is licensed under the AGPLv3 licence. Please see the [licence file](LICENSE) for more information.
[tl;dr](https://www.tldrlegal.com/license/gnu-affero-general-public-license-v3-agpl-3-0) you may copy, distribute and
modify ReVanced API as long as you track changes/dates in source files.
Any modifications to ReVanced API must also be made available under the GPL along with build & install instructions.
