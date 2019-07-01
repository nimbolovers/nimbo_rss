CREATE DATABASE  IF NOT EXISTS `nimbo_rss`;
USE `nimbo_rss`;

DROP TABLE IF EXISTS `content`;
DROP TABLE IF EXISTS `feed`;

CREATE TABLE `feed` (
                        `id` int(11) NOT NULL AUTO_INCREMENT,
                        `channel` varchar(45) DEFAULT NULL,
                        `title` text DEFAULT NULL,
                        `pub_date` date DEFAULT NULL,
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `content` (
                           `id` int(11) NOT NULL AUTO_INCREMENT,
                           `type` varchar(45) DEFAULT NULL,
                           `relation` varchar(45) DEFAULT NULL,
                           `mode` varchar(45) DEFAULT NULL,
                           `value` text DEFAULT NULL,
                           `feed_id` int(45) NOT NULL,
                           PRIMARY KEY (`id`),
                           FOREIGN KEY fk_cat(feed_id)
                               REFERENCES feed(id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;