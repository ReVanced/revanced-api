from pydantic import BaseModel


class AppInfoFields(BaseModel):
    """
    Fields for the AppInfo endpoint.
    """

    name: str
    category: str
    logo: str


class AppInfoModel(BaseModel):
    """
    Response model app info.
    """

    app_info: AppInfoFields
