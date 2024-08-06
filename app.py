from flask import Flask, render_template
from flask_nav import Nav
from flask_nav.elements import *
from sqlalchemy import create_engine, text
from collections import OrderedDict
import configparser


def create_app():
    nav = Nav()
    nav.register_element(
        'top',
        Navbar(
            View('Home', 'index'),
            View('Recommend Movies', 'recommend_movie'))
    )

    app = Flask(__name__)
    nav.init_app(app)

    config = configparser.ConfigParser()
    config.read('config.ini')

    host = config.get('Database', 'host')
    username = config.get('Database', 'username')
    password = config.get('Database', 'password')
    database = config.get('Database', 'database')

    engine = create_engine(f'mysql+pymysql://{username}:{password}@{host}/{database}')

    @app.route('/')
    def index():
        return render_template('index.html')

    @app.route('/recommend-movie')
    def recommend_movie():
        return render_template('recommendmovie.html')

    @app.route('/get-movie-recommendation', methods=['GET'])
    def get_movie_recommendation():
        genre = request.args.get('genre')
        tag = request.args.get('tag')

        sql = '''SELECT top_movies.title, top_movies.average_rating, g.genre_name, t.tag_name
        FROM ('''

        if genre == "" and tag == "":
            sql += '''
                SELECT m.movie_id, m.title, AVG(r.rating) AS average_rating
                FROM movies m
                JOIN ratings r ON m.movie_id = r.movie_id
                GROUP BY m.movie_id, m.title
                ORDER BY average_rating DESC
                LIMIT 10'''
        elif genre != "" and tag == "":
            sql += f'''
                SELECT hg.movie_id, avg_ratings.title, avg_ratings.average_rating
                FROM genre g
                JOIN has_genre hg ON g.genre_id = hg.genre_id
                JOIN (
                    SELECT m.movie_id, m.title, AVG(r.rating) AS average_rating
                    FROM movies m
                    JOIN ratings r ON m.movie_id = r.movie_id
                    GROUP BY m.movie_id, m.title
                ) as avg_ratings ON hg.movie_id = avg_ratings.movie_id
                WHERE g.genre_name = '{genre}'
                ORDER BY average_rating DESC
                LIMIT 10'''
        elif genre == "" and tag != "":
            sql += f'''
            SELECT ut.movie_id, avg_ratings.title, avg_ratings.average_rating, t.tag_name
            FROM tags t
            JOIN user_tags ut ON t.tag_id = ut.tag_id
            JOIN (
                SELECT m.movie_id, m.title, AVG(r.rating) AS average_rating
                FROM movies m
                JOIN ratings r ON m.movie_id = r.movie_id
                GROUP BY m.movie_id, m.title
            ) as avg_ratings ON ut.movie_id = avg_ratings.movie_id
            WHERE t.tag_name = 'dogs'
            ORDER BY average_rating DESC
            LIMIT 10'''
        elif genre != "" and tag != "":
            sql += f'''
            SELECT hg.movie_id, avg_ratings.title, avg_ratings.average_rating, g.genre_name
            FROM genre g
            JOIN has_genre hg ON g.genre_id = hg.genre_id
            JOIN user_tags ut ON hg.movie_id = ut.movie_id
            JOIN tags t ON ut.tag_id = t.tag_id
            JOIN (
                SELECT m.movie_id, m.title, AVG(r.rating) AS average_rating
                FROM movies m
                JOIN ratings r ON m.movie_id = r.movie_id
                GROUP BY m.movie_id, m.title
            ) as avg_ratings ON hg.movie_id = avg_ratings.movie_id
            WHERE g.genre_name = '{genre}' AND t.tag_name = '{tag}'
            ORDER BY average_rating DESC
            LIMIT 10'''

        sql += '''
        ) AS top_movies
        LEFT JOIN has_genre hg ON top_movies.movie_id = hg.movie_id
        LEFT JOIN genre g ON hg.genre_id = g.genre_id
        LEFT JOIN user_tags ut ON top_movies.movie_id = ut.movie_id
        LEFT JOIN tags t ON ut.tag_id = t.tag_id
        ORDER BY top_movies.average_rating DESC;'''

        movie_dict = OrderedDict()
        with engine.connect() as conn:
            result = conn.execute(text(sql))
            for row in result:
                if row[0] not in movie_dict:
                    movie_dict[row[0]] = (row[1], [row[2]], [row[3]])
                else:
                    _, genre, tags = movie_dict[row[0]]
                    if row[2] not in genre:
                        genre.append(row[2])
                    if row[3] not in tags:
                        tags.append(row[3])

        movies = []
        for title, (average_rating, genre, tags) in movie_dict.items():
            movies.append(
                (title, average_rating, ', '.join(str(genre) for genre in genre), ', '.join(str(tag) for tag in tags)))

        return render_template('recommendmovie.html', movies=movies)

    # Find average rating given genre and tag name
    @app.route('/get-average-rating', methods=['GET'])
    def get_avg_rating():
        genre = request.args.get('genre')
        tag = request.args.get('tag')

        sql = '''
        SELECT AVG(r.rating) AS average_rating
        FROM ratings r
        JOIN movies m ON r.movie_id = m.movie_id
        LEFT JOIN has_genre hg ON m.movie_id = hg.movie_id
        LEFT JOIN genre g ON hg.genre_id = g.genre_id
        LEFT JOIN user_tags ut ON m.movie_id = ut.movie_id
        LEFT JOIN tags t ON ut.tag_id = t.tag_id
        WHERE 1=1
        '''

        params = {}

        params = {}
        if genre:
            sql += ' AND g.genre_name = :genre'
            params['genre'] = genre
        if tag:
            sql += ' AND t.tag_name = :tag'
            params['tag'] = tag

        with engine.connect() as conn:
            result = conn.execute(text(sql), params)
            average_rating = result.scalar()

        return render_template('avg_rating.html', average_rating=average_rating, genre=genre, tag=tag)

    @app.route('/new-movie-insert', methods=['GET', 'POST'])
    def insert_stored_procedure():
        if request.method == 'GET':
            response = ""
        elif request.method == 'POST':
            title = request.form.get('title')
            year = request.form.get('year')
            imdb_picture_url = request.form.get('imdb_picture_url')
            director_name = request.form.get('director_name')

            if director_name != "" and title != "" and year != "" and imdb_picture_url != "":
                sql = f"CALL insert_movie_with_director('{director_name}', '{title}', {year}, '{imdb_picture_url}');"

                print(sql)

                with engine.connect() as conn:
                    result = conn.execute(text(sql))
                    conn.commit()
                    response = f"Success! Inserted movie {title} at id {result.first()[0]}!"
            else:
                response = "Error: Missing "
                response += "" if director_name != "" else "Director "
                response += "" if title != "" else "Title "
                response += "" if year != "" else "Year "
                response += "" if imdb_picture_url != "" else "IMDB Picture Url"

        return render_template('new_movie_insert.html', response=response)

    return app


if __name__ == '__main__':
    app = create_app()
    app.run(debug=True)
