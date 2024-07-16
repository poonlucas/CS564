CREATE DATABASE IF NOT EXISTS moviesdb;

USE moviesdb;
CREATE TABLE IF NOT EXISTS movies(
	movie_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    year INT,
    imdb_picture_url LONGTEXT,
    PRIMARY KEY (movie_id)
);

CREATE TABLE IF NOT EXISTS directors(
	director_id BIGINT NOT NULL AUTO_INCREMENT,
    director_name VARCHAR(255),
    PRIMARY KEY (director_id)
);

CREATE TABLE IF NOT EXISTS directs(
	director_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    FOREIGN KEY (director_id) REFERENCES directors(director_id),
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id)
);

CREATE TABLE IF NOT EXISTS genre(
	genre_id BIGINT NOT NULL AUTO_INCREMENT,
    genre_name VARCHAR(255),
    PRIMARY KEY (genre_id)
);

CREATE TABLE IF NOT EXISTS has_genre(
	genre_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    FOREIGN KEY (genre_id) REFERENCES genre(genre_id),
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id)
);

CREATE TABLE IF NOT EXISTS ratings(
    movie_id BIGINT NOT NULL,
    user_id BIGINT,
    rating INT NOT NULL,
    PRIMARY KEY (movie_id, user_id),
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id)
);

CREATE TABLE IF NOT EXISTS tags(
	tag_id BIGINT NOT NULL AUTO_INCREMENT,
    tag_name VARCHAR(255),
    PRIMARY KEY (tag_id)
);

CREATE TABLE IF NOT EXISTS user_tags(
	user_tag_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT,
    movie_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (user_tag_id),
    FOREIGN KEY (tag_id) REFERENCES tags(tag_id),
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id)
);
