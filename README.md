# ReVanced Releases API

---

![License: AGPLv3](https://img.shields.io/github/license/revanced/revanced-api)
[![Build and Publish Docker Image](https://github.com/revanced/revanced-api/actions/workflows/main.yml/badge.svg)](https://github.com/revanced/revanced-api/actions/workflows/main.yml)

---

This is a simple API that proxies requests needed to feed the ReVanced Manager and website with data.

## Usage

To run this API, you need Python 3.11.x. You can install the dependencies with poetry:

```shell
poetry install
```

Create the following environment variables:

- `GITHUB_TOKEN` with a valid GitHub token with read access to public repositories
- `SECRET_KEY` to salt login sessions
- `USERNAME` & `PASSWORD` to initialize the database with a user to login with to authenticated endpoints

Then, you can run the API in development mode with:

```shell
poetry run sanic app:app --dev
```

or in production mode with:

```shell
poetry run sanic app:app --fast
```

## Contributing

If you want to contribute to this project, feel free to open a pull request or an issue. We don't do much here, so it's pretty easy to contribute.

## License

This project is licensed under the AGPLv3 License - see the [LICENSE](LICENSE) file for details.
