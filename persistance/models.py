from sqlalchemy import Column, Integer, String, DateTime
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship
from sqlalchemy import ForeignKey

from persistance.database import Session, engine

Base = declarative_base()

Base.metadata.create_all(engine)


class AnnouncementDbModel(Base):
    __tablename__ = "announcements"

    id = Column(Integer, primary_key=True, autoincrement=True)
    author = Column(String, nullable=True)
    title = Column(String, nullable=False)
    message = Column(String, nullable=True)
    created_at = Column(DateTime, nullable=False)
    channel_id = Column(Integer, nullable=True)

    attachments = relationship("AttachmentDbModel", back_populates="announcements")


class AttachmentDbModel(Base):
    __tablename__ = "attachments"

    id = Column(Integer, primary_key=True, autoincrement=True)
    announcement_id = Column(Integer, ForeignKey("announcements.id"))
    attachment_url = Column(String, nullable=False)

    announcements = relationship("AnnouncementDbModel", back_populates="attachments")


class UserDbModel(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, autoincrement=True)
    username = Column(String, nullable=False)
    password = Column(String, nullable=False)

    # Required by sanic-beskar
    @property
    def rolenames(self):
        return []

    @classmethod
    async def lookup(cls, username=None):
        try:
            session = Session()

            user = session.query(UserDbModel).filter_by(username=username).first()

            session.close()

            return user
        except:
            return None

    @classmethod
    async def identify(cls, id):
        try:
            session = Session()

            user = session.query(UserDbModel).filter_by(id=id).first()

            session.close()

            return user
        except:
            return None

    @property
    def identity(self):
        return self.id
