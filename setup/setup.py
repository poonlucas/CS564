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

    os.remove("temp.zip")


def read_dats():
    for file in FILES:
        df = pd.read_csv(file, delimiter="\t", on_bad_lines='warn', encoding='ISO-8859-1')
        print(df)
        os.remove(file)


if __name__ == '__main__':
    download_zip()
    read_dats()
