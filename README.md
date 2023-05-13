# ReVanced Releases API

---

![License: AGPLv3](https://img.shields.io/github/license/revanced/revanced-api)
[![Qodana | Code Quality Scan](https://github.com/revanced/revanced-api/actions/workflows/quodana.yml/badge.svg)](https://github.com/revanced/revanced-api/actions/workflows/quodana.yml)
[![MyPy | Static Type Checking](https://github.com/revanced/revanced-api/actions/workflows/mypy.yml/badge.svg)](https://github.com/revanced/revanced-api/actions/workflows/mypy.yml)

---

This is a simple API that proxies requests needed to feed the ReVanced Manager and website with data.

## Usage

To run this API, you will need to have Python 3.11.x installed. You can install the dependencies with poetry:

```shell
poetry install
```

Create an environment variable called `GITHUB_TOKEN` with a valid GitHub token with read access to public repositories.

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
