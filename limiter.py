from sanic_limiter import Limiter, get_remote_address

limiter = Limiter(key_func=get_remote_address)


def configure_limiter(app):
    limiter.init_app(app)
