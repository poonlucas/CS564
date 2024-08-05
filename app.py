from flask import Flask, render_template
from flask_nav import Nav
from flask_nav.elements import *
from sqlalchemy import create_engine, text


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

    host = 'localhost'
    username = 'root'
    password = 'root'
    database = 'moviesdb'

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
        sql = f'''
        SELECT avg_ratings.title, avg_ratings.average_rating, g.genre_name
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
        LIMIT 10;
        '''
        with engine.connect() as conn:
            result = conn.execute(text(sql))
            print(result)
            movies = result

        return render_template('recommendmovie.html', movies=movies)

    return app


if __name__ == '__main__':
    create_app().run(debug=True)
