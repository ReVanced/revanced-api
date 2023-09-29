import os
import secrets
import string
from persistance.database import Session

from sanic_beskar import Beskar

from persistance.models import UserDbModel

beskar = Beskar()


def configure_auth(app):
    app.config.SECRET_KEY = os.environ.get("SECRET_KEY").join(
        secrets.choice(string.ascii_letters) for i in range(15))
    app.config["TOKEN_ACCESS_LIFESPAN"] = {"hours": 24}
    app.config["TOKEN_REFRESH_LIFESPAN"] = {"days": 30}
    beskar.init_app(app, UserDbModel)

    _init_default_user()


def _init_default_user():
    username = os.environ.get("USERNAME")
    password = os.environ.get("PASSWORD")

    if not username or not password:
        raise Exception("Missing USERNAME or PASSWORD environment variables")

    session = Session()

    existing_user = session.query(
        UserDbModel).filter_by(username=username).first()
    if not existing_user:
        session.add(UserDbModel(username=username,
                    password=beskar.hash_password(password)))
        session.commit()

    session.close()
