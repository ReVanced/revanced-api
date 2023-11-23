#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os
import re
import fire
import config
import pkgutil
import importlib
from loguru import logger
from cytoolz import map, valmap, filter

import api.utils.cli as cli_utils

try:
    import uvloop

    uvloop.install()
except ImportError:
    logger.info("Could not import uvloop. Using asyncio event loop.")

try:
    backend = importlib.import_module(f"api.backends.{config.backend}")
except ModuleNotFoundError:
    logger.error(f"Could not import api/backends/{config.backend}.py. Exiting.")
    quit()


class CLI(object):
    """
    CLI for the ReVanced API.
    """

    __dir_structure: dict[str, list[str]] = {}
    __filtered_repositories: list[str] = []
    __imported_methods: list[str] = []

    def __init__(self):
        self.__dir_structure = self.__gen_dir_structure()
        self.__filtered_repositories = self.__gen_filtered_repositories()
        for _, module_name, ispkg in pkgutil.iter_modules(["api"]):
            if not ispkg:
                module = importlib.import_module(f"api.{module_name}")
                for attr_name in dir(module):
                    attr = getattr(module, attr_name)
                    if (
                        callable(attr)
                        and hasattr(attr, "is_cli_helper")
                        and attr.is_cli_helper
                    ):
                        setattr(self, attr.cli_name or attr_name, attr)
                        self.__imported_methods.append(attr.cli_name or attr_name)

    def helper(self, name=None):
        def decorator(func):
            func.is_cli_helper = True
            func.cli_name = name
            return func

        return decorator

    def __generate_index(self, path: str):
        """
        Generate the index file for the static API.
        """
        with open(f"{path}/index.html", "w") as file:
            file.write(cli_utils.index_model)

    def __extract_routes(self, module_name: str) -> list[dict[str, str]]:
        pattern = (
            rf'@{module_name}\.(get|post|put|delete|patch|head)\(["\'](/[^"\']+)["\']\)'
        )
        try:
            with open(f"api/{module_name}.py", "r") as file:
                content = file.read()
                return list(
                    map(
                        lambda match: match[1],
                        re.findall(pattern, content, re.MULTILINE),
                    )
                )
        except FileNotFoundError:
            logger.error(f"api/{module_name}.py not found.")

    def __gen_dir_structure(self) -> dict[str, list[str]]:
        filtered_api_versions = valmap(
            lambda lst: [
                item for item in lst if item not in config.static_api_ignore_routes
            ],
            config.api_versions,
        )

        return {
            version: {
                endpoint: self.__extract_routes(endpoint) for endpoint in endpoints
            }
            for version, endpoints in filtered_api_versions.items()
        }

    def __gen_filtered_repositories(self) -> list[str]:
        return list(
            filter(
                lambda repo: repo not in config.static_api_ignore_repos,
                config.compat_repositories,
            )
        )

    def get_filtered_repositories(self) -> list[str]:
        return self.__filtered_repositories

    def get_file_path(self, search_path: str) -> str:
        """
        Get the path inside of the scaffolding directory for a module.

        Args:
            module_name (str): The name of the module.

        Returns:
            str: The path inside of the scaffolding directory for the module, including the version.
        """
        for version, categories in self.__dir_structure.items():
            for _, paths in categories.items():
                if search_path in paths:
                    if version == "old":
                        return f"out{search_path}"

                    return f"out/{version}{search_path}"
        return None

    def scaffold(self):
        """
        Scaffold the static API directory structure.
        """

        if os.path.exists("./out"):
            logger.error(
                "The output directory for the static API already exists. Exiting."
            )
            quit()
        else:
            os.mkdir("./out")
            logger.info("Output directory created.")

        logger.info("Scaffolding static API directory structure.")

        for version, endpoints in self.__dir_structure.items():
            if version == "old":
                logger.info("Creating directories for compatibility layer.")
                for route in endpoints["compat"]:
                    directory = f"./out{route}"
                    os.mkdir(directory)
            else:
                os.mkdir(f"./out/{version}")
                logger.info(f"Creating directories for {version}")
                for module, routes in endpoints.items():
                    logger.info(f"Creating directories for {version}/{module}")
                    for route in routes:
                        if "<repo:str>" in route:
                            if "<tag:str>" in route:
                                for repository in self.__filtered_repositories:
                                    directory = f"./out/{version}{route.replace('<repo:str>', repository).rstrip('<tag:str>')}"
                                    os.makedirs(
                                        directory,
                                        exist_ok=True,
                                    )
                            else:
                                for repository in self.__filtered_repositories:
                                    directory = f"./out/{version}{route.replace('<repo:str>', repository)}"
                                    os.makedirs(
                                        directory,
                                        exist_ok=True,
                                    )
                        if "<tag:str>" in route:
                            directory = f"./out/{version}{route.rstrip('<tag:str>')}"
                            if "<repo:str>" not in directory:
                                os.makedirs(
                                    directory,
                                    exist_ok=True,
                                )
                        else:
                            directory = f"./out/{version}{route}"
                            if "<repo:str>" not in directory:
                                os.makedirs(
                                    directory,
                                    exist_ok=True,
                                )

    def list(self):
        """
        List all available CLI helpers.
        """
        logger.info("Available CLI helpers:")
        for method in self.__imported_methods:
            logger.info(f"- {method}")


if __name__ == "__main__":
    fire.Fire(CLI)
