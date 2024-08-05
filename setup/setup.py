import os
import urllib.request
import zipfile
import pandas as pd
from sqlalchemy import create_engine
import configparser


config = configparser.ConfigParser()
config.read('config.ini')


host = config.get('Database', 'host')
username = config.get('Database', 'username')
password = config.get('Database', 'password')
database = config.get('Database', 'database')

FILES = ["user_taggedmovies-timestamps.dat",
         "movie_directors.dat",
         "movie_genres.dat",
         "movies.dat",
         "tags.dat",
         "user_ratedmovies-timestamps.dat",
         "user_taggedmovies.dat"]
dfs = dict()


def download_zip():
    print("Downloading dataset...")
    urllib.request.urlretrieve(
        "https://files.grouplens.org/datasets/hetrec2011/hetrec2011-movielens-2k-v2.zip", "temp.zip")

    print("Unzipping dataset...")
    with zipfile.ZipFile("temp.zip") as zf:
        for file in zf.infolist():
            if file.filename in FILES:
                zf.extract(file)

    os.remove("temp.zip")


def read_dats():
    print("Reading dataset...")
    for file in FILES:
        df = pd.read_csv(file, delimiter="\t", on_bad_lines='warn', encoding='ISO-8859-1')
        dfs[file] = df
        os.remove(file)


def insert():

    engine = create_engine(f'mysql+pymysql://{username}:{password}@{host}/{database}')

    print("Populating directors...")
    directors_dict = {k: v for v, k in enumerate(dfs[FILES[1]].directorName.unique())}
    directors = pd.DataFrame(directors_dict.items())[[1, 0]]. \
        rename(columns={1: 'director_id', 0: 'director_name'})
    directors.to_sql('directors', engine, if_exists='append', index=False)

    print("Populating tags...")
    tags = dfs[FILES[4]]. \
        rename(columns={'id': 'tag_id', 'value': 'tag_name'})
    tags.to_sql('tags', engine, if_exists='append', index=False)

    print("Populating movies...")
    movies = dfs[FILES[3]][['id', 'title', 'year', 'imdbPictureURL']]. \
        rename(columns={'id': 'movie_id', 'imdbPictureURL': 'imdb_picture_url'})
    movies.to_sql('movies', engine, if_exists='append', index=False)

    print("Populating directs...")
    dfs[FILES[1]]['director_id'] = dfs[FILES[1]]['directorName'].map(directors_dict)
    directs = dfs[FILES[1]][['director_id', 'movieID']]. \
        rename(columns={'movieID': 'movie_id'})
    directs.to_sql('directs', engine, if_exists='append', index=False)

    print("Populating user_tags...")
    user_tags = dfs[FILES[0]]. \
        drop(columns=['timestamp']). \
        rename(columns={'userID': 'user_id', 'movieID': 'movie_id', 'tagID': 'tag_id'})
    user_tags.to_sql('user_tags', engine, if_exists='append', index=False)

    print("Populating ratings...")
    ratings = dfs[FILES[5]][['userID', 'movieID', 'rating']]. \
        rename(columns={'userID': 'user_id', 'movieID': 'movie_id'})
    ratings.to_sql('ratings', engine, if_exists='append', index=False)

    print("Populating genre...")
    genre_dict = {k: v for v, k in enumerate(dfs[FILES[2]].genre.unique())}
    genre = pd.DataFrame(genre_dict.items())[[1, 0]]. \
        rename(columns={1: 'genre_id', 0: 'genre_name'})
    genre.to_sql('genre', engine, if_exists='append', index=False)

    print("Populating has_genre...")
    dfs[FILES[2]]['genre_id'] = dfs[FILES[2]]['genre'].map(genre_dict)
    has_genre = dfs[FILES[2]][['genre_id', 'movieID']]. \
        rename(columns={'movieID': 'movie_id'})
    has_genre.to_sql('has_genre', engine, if_exists='append', index=False)


if __name__ == '__main__':
    download_zip()
    read_dats()
    insert()
    print("Setup complete.")
