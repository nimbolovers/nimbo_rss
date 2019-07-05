-- USE `nimbo_rss`;

DROP TABLE IF EXISTS `site`;
DROP TABLE IF EXISTS `description`;
DROP TABLE IF EXISTS `content`;
DROP TABLE IF EXISTS `feed`;

CREATE TABLE `feed` (
                        `id` int(11) NOT NULL AUTO_INCREMENT,
                        `channel` TEXT DEFAULT NULL,
                        `title` TEXT DEFAULT NULL,
                        `link` TEXT DEFAULT NULL,
                        `pub_date` date DEFAULT NULL,
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `description` (
                               `id` int(11) NOT NULL AUTO_INCREMENT,
                               `type` varchar(45) DEFAULT NULL,
                               `mode` varchar(45) DEFAULT NULL,
                               `value` text DEFAULT NULL,
                               `feed_id` int(45) NOT NULL,
                               PRIMARY KEY (`id`),
                               FOREIGN KEY (feed_id)
                                   REFERENCES feed(id)
                                   ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `content` (
                           `id` int(11) NOT NULL AUTO_INCREMENT,
                           `value` text DEFAULT NULL,
                           `feed_id` int(45) NOT NULL,
                           PRIMARY KEY (`id`),
                           FOREIGN KEY (feed_id)
                               REFERENCES feed(id)
                               ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `site` (
                        `id` int(11) NOT NULL AUTO_INCREMENT,
                        `name` varchar(45) DEFAULT NULL,
                        `link` text NOT NULL,
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

INSERT INTO site(name, link) VALUES('navad', 'https://90tv.ir/rss/news');
INSERT INTO site(name, link) VALUES('tabnak', 'https://www.tabnak.ir/fa/rss/allnews');
