import os
import urllib.request
import zipfile
import pandas as pd

FILES = ["user_taggedmovies-timestamps.dat",
         "movie_directors.dat",
         "movie_genres.dat",
         "movies.dat",
         "tags.dat",
         "user_ratedmovies-timestamps.dat",
         "user_taggedmovies.dat"]


def download_zip():
    urllib.request.urlretrieve(
        "https://files.grouplens.org/datasets/hetrec2011/hetrec2011-movielens-2k-v2.zip", "temp.zip")

    with zipfile.ZipFile("temp.zip") as zf:
        for file in zf.infolist():
            if file.filename in FILES:
                zf.extract(file)


def read_dats():
    for file in FILES:
        df = pd.read_csv(file)
        print(df)


def cleanup():
    os.remove("temp.zip")
    for file in FILES:
        os.remove(file)


if __name__ == '__main__':
    download_zip()
    read_dats()
    cleanup()
