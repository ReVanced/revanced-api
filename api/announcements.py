"""
This module provides a blueprint for the announcements endpoint.

Routes:
    - GET /announcements: Get a list of announcements from all channels.
    - GET /announcements/<channel:str>: Get a list of announcement from a channel.
    - GET /announcements/latest: Get the latest announcement.
    - GET /announcements/<channel:str>/latest: Get the latest announcement from a channel.
    - POST /announcements/<channel:str>: Create an announcement.
    - DELETE /announcements/<announcement_id:int>: Delete an announcement.
"""

import datetime
from sanic import Blueprint, Request
from sanic.response import JSONResponse, json
from sanic_ext import openapi
from data.database import Session
from data.models import AnnouncementDbModel, AttachmentDbModel

import sanic_beskar

from api.models.announcements import AnnouncementResponseModel
from config import api_version
from api.limiter import limiter

announcements: Blueprint = Blueprint("announcements", version=api_version)


@announcements.get("/announcements")
@openapi.definition(
    summary="Get a list of announcements",
    response=[[AnnouncementResponseModel]],
)
async def get_announcements(request: Request) -> JSONResponse:
    """
    Retrieve a list of announcements.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing a list of announcements from all channels.
    """

    session = Session()

    announcements = [
        AnnouncementResponseModel.to_response(announcement)
        for announcement in session.query(AnnouncementDbModel).all()
    ]

    session.close()

    return json(announcements, status=200)


@announcements.get("/announcements/<channel:str>")
@openapi.definition(
    summary="Get a list of announcements from a channel",
    response=[[AnnouncementResponseModel]],
)
async def get_announcements_for_channel(request: Request, channel: str) -> JSONResponse:
    """
    Retrieve a list of announcements from a channel.

    **Args:**
        - channel (str): The channel to retrieve announcements from.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing a list of announcements from a channel.
    """

    session = Session()

    announcements = [
        AnnouncementResponseModel.to_response(announcement)
        for announcement in session.query(AnnouncementDbModel)
        .filter_by(channel=channel)
        .all()
    ]

    session.close()

    return json(announcements, status=200)


@announcements.get("/announcements/latest")
@openapi.definition(
    summary="Get the latest announcement",
    response=AnnouncementResponseModel,
)
async def get_latest_announcement(request: Request) -> JSONResponse:
    """
    Retrieve the latest announcement.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing the latest announcement.
    """

    session = Session()

    announcement = (
        session.query(AnnouncementDbModel)
        .order_by(AnnouncementDbModel.id.desc())
        .first()
    )

    if not announcement:
        return json({"error": "No announcement found"}, status=404)

    announcement_response = AnnouncementResponseModel.to_response(announcement)

    session.close()

    return json(announcement_response, status=200)


# for specific channel


@announcements.get("/announcements/<channel:str>/latest")
@openapi.definition(
    summary="Get the latest announcement from a channel",
    response=AnnouncementResponseModel,
)
async def get_latest_announcement_for_channel(
    request: Request, channel: str
) -> JSONResponse:
    """
    Retrieve the latest announcement from a channel.

    **Args:**
        - channel (str): The channel to retrieve the latest announcement from.

    **Returns:**
        - JSONResponse: A Sanic JSONResponse object containing the latest announcement from a channel.
    """

    session = Session()

    announcement = (
        session.query(AnnouncementDbModel)
        .filter_by(channel=channel)
        .order_by(AnnouncementDbModel.id.desc())
        .first()
    )

    if not announcement:
        return json({"error": "No announcement found"}, status=404)

    announcement_response = AnnouncementResponseModel.to_response(announcement)

    session.close()

    return json(announcement_response, status=200)


@announcements.post("/announcements/<channel:str>")
@limiter.limit("16 per hour")
@sanic_beskar.auth_required
@openapi.definition(
    summary="Create an announcement",
    body=AnnouncementResponseModel,
    response=AnnouncementResponseModel,
)
async def post_announcement(request: Request, channel: str) -> JSONResponse:
    """
    Create an announcement.

    **Args:**
        - author (str | None): The author of the announcement.
        - title (str): The title of the announcement.
        - content (ContentFields | None): The content of the announcement.
        - channel (str): The channel to create the announcement in.
        - nevel (int | None): The severity of the announcement.
    """
    session = Session()

    if not request.json:
        return json({"error": "Missing request body"}, status=400)

    content = request.json.get("content", None)

    author = request.json.get("author", None)
    title = request.json.get("title")
    message = content["message"] if content and "message" in content else None
    attachments = (
        list(
            map(
                lambda url: AttachmentDbModel(attachment_url=url),
                content["attachments"],
            )
        )
        if content and "attachments" in content
        else []
    )
    level = request.json.get("level", None)
    created_at = datetime.datetime.now()

    announcement = AnnouncementDbModel(
        author=author,
        title=title,
        message=message,
        attachments=attachments,
        channel=channel,
        created_at=created_at,
        level=level,
    )

    session.add(announcement)
    session.commit()
    session.close()

    return json({}, status=200)


@announcements.delete("/announcements/<announcement_id:int>")
@sanic_beskar.auth_required
@openapi.definition(
    summary="Delete an announcement",
)
async def delete_announcement(request: Request, announcement_id: int) -> JSONResponse:
    """
    Delete an announcement.

    **Args:**
        - announcement_id (int): The ID of the announcement to delete.

    **Exceptions:**
        - 404: Announcement not found.
    """
    session = Session()

    announcement = (
        session.query(AnnouncementDbModel).filter_by(id=announcement_id).first()
    )

    if not announcement:
        return json({"error": "Announcement not found"}, status=404)

    session.delete(announcement)
    session.commit()
    session.close()

    return json({}, status=200)
