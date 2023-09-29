from persistance.models import AnnouncementDbModel


class ContentFields(dict):
    message: str | None
    attachment_urls: list[str] | None


class AnnouncementResponseModel(dict):
    id: int
    author: str | None
    title: str
    content: ContentFields | None
    channel_id: int
    created_at: str

    @staticmethod
    def to_response(announcement: AnnouncementDbModel):
        response = AnnouncementResponseModel(
            id=announcement.id,
            author=announcement.author,
            title=announcement.title,
            created_at=str(announcement.created_at),
            content=ContentFields(
                message=announcement.message,
                attachment_urls=[
                    attachment.attachment_url for attachment in announcement.attachments
                ],
            )
            if announcement.message or announcement.attachments
            else None,
            channel_id=announcement.channel_id,
        )

        return response
