FROM python:3.11-slim

ARG GITHUB_TOKEN
ENV GITHUB_TOKEN $GITHUB_TOKEN

WORKDIR /usr/src/app

COPY . .

VOLUME persistance

CMD [ "python3", "-m" , "sanic", "app:app", "--fast", "--access-logs", "--motd", "--noisy-exceptions", "-H", "0.0.0.0"]
