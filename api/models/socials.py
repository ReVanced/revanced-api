from pydantic import BaseModel


class Socials(BaseModel):
    socials: dict[str, str]
