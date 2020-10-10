load data infile 'D:/Capstone/2020_2/data/users.csv'
    into table `user`
    fields terminated by ','
    enclosed by '"'
    lines terminated by '\r\n'
    ignore 1 rows
(user_id, user_pw, `name`, nick_name);

select * from `user`;

load data infile 'D:/Capstone/2020_2/data/tattoists.csv'
    into table tattooist
    fields terminated by ','
    enclosed by '"'
    lines terminated by '\r\n'
    ignore 1 rows;

select * from tattooist;

load data infile 'D:/Capstone/2020_2/data/posts.csv'
    into table post
    fields terminated by ','
    enclosed by '"'
    lines terminated by '\r\n'
    ignore 1 rows
(tattooist_id, title, `description`, price, like_num, genre, big_shape, small_shape, design_url, avg_clean_score);

select * from post;

load data infile 'D:/Capstone/2020_2/data/images.csv'
    into table tattoo_image
    fields terminated by ','
    enclosed by '"'
    lines terminated by '\r\n'
    ignore 1 rows
(post_id, filename, url);

select * from tattoo_image;


