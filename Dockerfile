FROM python:3.11-slim

ARG GITHUB_TOKEN
ARG SENTRY_DSN

ENV GITHUB_TOKEN $GITHUB_TOKEN
ENV SENTRY_DSN $SENTRY_DSN

WORKDIR /usr/src/app

COPY . .

RUN apt update && \
    apt-get install git build-essential libffi-dev libssl-dev openssl --no-install-recommends -y \
    && pip install --no-cache-dir -r requirements.txt

VOLUME persistence

CMD [ "python3", "-m" , "sanic", "app:app", "--fast", "--access-logs", "--motd", "--noisy-exceptions", "-H", "0.0.0.0"]
