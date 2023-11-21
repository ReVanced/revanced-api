# api/__init__.py
from sanic import Blueprint
import importlib
import pkgutil

blueprints = []
for _, module_name, _ in pkgutil.iter_modules(["api"]):
    # Import the module
    module = importlib.import_module(f"api.{module_name}")

    # Add the module's blueprint to the list, if it exists
    if hasattr(module, module_name):
        blueprints.append(getattr(module, module_name))

# Create the Blueprint group with the dynamically imported blueprints
api = Blueprint.group(*blueprints, url_prefix="/")
