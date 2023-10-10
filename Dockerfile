## Build dependencies
FROM python:3.11-slim as dependencies

WORKDIR /usr/src/app

RUN apt-get update && \
    apt-get install -y --no-install-recommends gcc \
    && rm -rf /var/lib/apt/lists/*

RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

COPY requirements.txt .

RUN pip install -r requirements.txt

## Image
FROM python:3.11-slim

WORKDIR /usr/src/app

RUN apt-get update && \
    apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

ENV PATH="/opt/venv/bin:$PATH"

COPY --from=dependencies /opt/venv /opt/venv
COPY . .

VOLUME persistance


CMD docker/run-backend.sh
HEALTHCHECK CMD docker/run-healthcheck.sh

EXPOSE 8000
