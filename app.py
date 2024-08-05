from flask import Flask, render_template
from flask_nav import Nav
from flask_nav.elements import *


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

    @app.route('/')
    def index():
        return render_template('index.html')

    @app.route('/recommend-movie')
    def recommend_movie():
        return render_template('recommendmovie.html')

    @app.route('/get-movie-recommendation', methods=['GET'])
    def get_movie_recommendation():
        genre = request.form.get('genre')
        movies = [['Return of the Living Dead Part II', '5.0000']]
        return render_template('recommendmovie.html', movies=movies)

    return app


if __name__ == '__main__':
    create_app().run(debug=True)
