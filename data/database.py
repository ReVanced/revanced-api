from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

engine = create_engine("sqlite:///persistence/database.db", pool_size=20)

Session = sessionmaker(bind=engine)
