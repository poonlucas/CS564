CREATE DATABASE IF NOT EXISTS moviesdb;

USE moviesdb;
CREATE TABLE IF NOT EXISTS movies(
	movie_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    year INT,
    imdb_picture_url LONGTEXT,
    PRIMARY KEY (movie_id)
);

CREATE TABLE IF NOT EXISTS directors(
	director_id BIGINT NOT NULL,
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
	genre_id BIGINT NOT NULL,
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
	tag_id BIGINT NOT NULL,
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

DELIMITER //
CREATE PROCEDURE insert_movie_with_director(IN director_name VARCHAR(255), IN title VARCHAR(255), IN year INT, IN imdb_picture_url VARCHAR(255))
BEGIN
	DECLARE n_director_id BIGINT;
    DECLARE n_movie_id BIGINT;

    SELECT d.director_id INTO n_director_id
    FROM directors d
    WHERE d.director_name = director_name;

    IF n_director_id IS NULL THEN
		SET n_director_id = (SELECT MAX(director_id) FROM directors);
        INSERT INTO directors VALUES (n_director_id, director_name);
	END IF;

    SET n_movie_id = (SELECT MAX(m.movie_id) + 1 FROM movies m);
    INSERT INTO movies VALUES (n_movie_id, title, year, imdb_picture_url);
    INSERT INTO directs VALUES (n_director_id, n_movie_id);
    SELECT n_movie_id;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE delete_movie(IN movie_id BIGINT)
BEGIN
	DELETE FROM has_genre hg WHERE hg.movie_id = movie_id;
    DELETE FROM directs dir WHERE dir.movie_id = movie_id;
    DELETE FROM user_tags ut WHERE ut.movie_id = movie_id;
    DELETE FROM movies m WHERE m.movie_id = movie_id;
    SELECT * FROM movies m WHERE m.movie_id = movie_id;
END //
DELIMITER ;



