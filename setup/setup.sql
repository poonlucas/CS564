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
CREATE PROCEDURE get_average_rating(IN title VARCHAR(255))
BEGIN
	SELECT movie.title, movie.year, movie.average_rating, g.genre_name, t.tag_name
	FROM (
		SELECT m.movie_id, m.title, m.year, AVG(r.rating) AS average_rating
		FROM movies m
		JOIN ratings r ON m.movie_id = r.movie_id
		WHERE m.title = title
		GROUP BY m.movie_id, m.title, m.imdb_picture_url
	) AS movie
	LEFT JOIN has_genre hg ON movie.movie_id = hg.movie_id
	LEFT JOIN genre g ON hg.genre_id = g.genre_id
	LEFT JOIN user_tags ut ON movie.movie_id = ut.movie_id
	LEFT JOIN tags t ON ut.tag_id = t.tag_id;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE insert_movie(IN director_name VARCHAR(255), IN title VARCHAR(255), IN year INT, IN imdb_picture_url VARCHAR(255), IN genre_name VARCHAR(255))
BEGIN
	DECLARE n_director_id BIGINT;
    DECLARE n_genre_id BIGINT;
    DECLARE n_movie_id BIGINT;

    SELECT d.director_id INTO n_director_id
    FROM directors d
    WHERE d.director_name = director_name;

    SELECT g.genre_id INTO n_genre_id
    FROM genre g
    WHERE g.genre_name = genre_name;

    IF n_director_id IS NULL THEN
		SET n_director_id = (SELECT MAX(director_id) FROM directors);
        INSERT INTO directors VALUES (n_director_id, director_name);
	END IF;

    IF n_genre_id IS NULL THEN
		SET n_genre_id = (SELECT MAX(genre_id) FROM genre);
        INSERT INTO genre VALUES (n_genre_id, genre_name);
	END IF;

    SET n_movie_id = (SELECT MAX(m.movie_id) + 1 FROM movies m);
    INSERT INTO movies VALUES (n_movie_id, title, year, imdb_picture_url);
    INSERT INTO directs VALUES (n_director_id, n_movie_id);
    INSERT INTO has_genre VALUES (n_genre_id, n_movie_id);
    SELECT n_movie_id;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE delete_movie(IN movie_id BIGINT)
BEGIN
	DECLARE movie_title VARCHAR(255);
    SET movie_title = (SELECT m.title FROM movies m WHERE m.movie_id = movie_id);

	DELETE FROM has_genre hg WHERE hg.movie_id = movie_id;
    DELETE FROM directs dir WHERE dir.movie_id = movie_id;
    DELETE FROM user_tags ut WHERE ut.movie_id = movie_id;
    DELETE FROM ratings r WHERE r.movie_id = movie_id;
    DELETE FROM movies m WHERE m.movie_id = movie_id;
    SELECT movie_title;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE rate(IN movie_id BIGINT, IN user_id BIGINT, IN rating INT)
BEGIN
	DECLARE movie_rows INT;
    DECLARE count_rows INT;

    SELECT COUNT(*) INTO movie_rows
    FROM movies m
    WHERE m.movie_id = movie_id;

    IF movie_rows < 1 THEN
		SELECT 'Movie does not exist', 0;
	ELSE
		SELECT COUNT(*) INTO count_rows
		FROM ratings r
		WHERE r.movie_id = movie_id AND r.user_id = user_id;

		IF count_rows > 0 THEN
			SELECT 'User already rated', 0;
		ELSE
			INSERT INTO ratings VALUES (movie_id, user_id, rating);
            SELECT m.title, 1 FROM movies m WHERE m.movie_id = movie_id;
		END IF;
	END IF;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE tag(IN movie_id BIGINT, IN user_id BIGINT, IN tag_name VARCHAR(255))
BEGIN
	DECLARE n_tag_id BIGINT;
	DECLARE movie_rows INT;
    DECLARE count_rows INT;

    SELECT COUNT(*) INTO movie_rows
    FROM movies m
    WHERE m.movie_id = movie_id;

    SELECT t.tag_id INTO n_tag_id
    FROM tags t
    WHERE t.tag_name = tag_name;

    IF movie_rows < 1 THEN
		SELECT 'Movie does not exist', 0;
	ELSE
		SELECT COUNT(*) INTO count_rows
		FROM user_tags ut
		WHERE ut.movie_id = movie_id AND ut.user_id = user_id AND ut.tag_id = n_tag_id;

		IF count_rows > 0 THEN
			SELECT 'User already gave this tag to this movie', 0;
		ELSE
			IF n_tag_id IS NULL THEN
				SET n_tag_id = (SELECT MAX(tag_id) + 1 FROM tags);
				INSERT INTO tags VALUES (n_tag_id, tag_name);
			END IF;

			INSERT INTO user_tags (user_id, movie_id, tag_id) VALUES (user_id, movie_id, n_tag_id);
            SELECT m.title, 1 FROM movies m WHERE m.movie_id = movie_id;
		END IF;
	END IF;
END //
DELIMITER ;



