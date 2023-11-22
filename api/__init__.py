# api/__init__.py
from sanic import Blueprint
import importlib
import pkgutil
from api.utils.versioning import get_version

# Dynamically import all modules in the 'api' package, excluding subdirectories
versioned_blueprints = {}
for finder, module_name, ispkg in pkgutil.iter_modules(["api"]):
    if not ispkg:
        # Import the module
        module = importlib.import_module(f"api.{module_name}")

        # Add the module's blueprint to the versioned list, if it exists
        if hasattr(module, module_name):
            blueprint = getattr(module, module_name)
            version = get_version(module_name)
            versioned_blueprints.setdefault(version, []).append(blueprint)

# Create Blueprint groups for each version
api = []
for version, blueprints in versioned_blueprints.items():
    if version == "old":
        group = Blueprint.group(*blueprints, url_prefix="/")
    else:
        group = Blueprint.group(*blueprints, version=version, url_prefix="/")
    api.append(group)
