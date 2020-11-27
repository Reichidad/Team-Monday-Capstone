CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` varchar(20) NOT NULL,
  `user_pw` varchar(20) NOT NULL,
  `name` varchar(20) NOT NULL,
  `nick_name` varchar(20) not null,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tattooist` (
  `id` int NOT NULL,
  `nick_name` varchar(20) not null,
  `big_address` varchar(50) NOT NULL,
  `small_address` varchar(50) NOT NULL,
  `mobile` varchar(30) NOT NULL,
  `description` varchar(50) NOT NULL,
  PRIMARY KEY (id),
  foreign key (id) references user(id) on delete restrict
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `post` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tattooist_id` int NOT NULL,
  `title` varchar(30) NOT NULL,
  `description` varchar(200),
  `price` int not null,
  `like_num` int,
  `genre` varchar(20) not null,
  `big_shape` varchar(20) not null,
  `small_shape` varchar(20) not null,
  `design_url` varchar(80),
  `avg_clean_score` float,
  foreign key (tattooist_id) references user(id) on delete restrict,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `review` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `post_id` int NOT NULL,
  `nick_name` varchar(20) NOt NULL,
  `date` datetime not null,
  `description` varchar(50),
  `tattoo_url1` varchar(80) not null,
  `tattoo_url2` varchar(80) not null,
  foreign key (user_id) references user(id) on delete restrict,
  foreign key (post_id) references post(id) on delete restrict,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tattoo_image` (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NOT NULL,
  `filename` varchar(50) not null,
  `url` varchar(80) not null,
  foreign key (post_id) references post(id) on delete restrict,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
