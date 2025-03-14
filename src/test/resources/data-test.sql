---------------------------------
-- Data for H2
---------------------------------

INSERT INTO book (author, description, isbn, title, url) VALUES
(NULL, NULL, 'isbn', 'title1', 'url1'),
(NULL, NULL, 'isbn2', 'title2', 'url2'),
(NULL, NULL, 'isbn3', 'title3', 'url3'),
(NULL, NULL, 'isbn4', 'title4', 'url4');

INSERT INTO category (id, name) VALUES
(1, 'science'),
(3, 'art');

INSERT INTO book_category (category_id, book_id) VALUES
(1, 'isbn'),
(1, 'isbn2'),
(1, 'isbn3'),
(1, 'isbn4'),
(3, 'isbn4');
