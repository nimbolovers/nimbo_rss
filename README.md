# RSS Service [![Build Status](https://travis-ci.com/karimiehsan90/nimbo_rss.svg?branch=master)](https://travis-ci.com/karimiehsan90/nimbo_rss) [![codecov](https://codecov.io/gh/karimiehsan90/nimbo_rss/branch/master/graph/badge.svg)](https://codecov.io/gh/karimiehsan90/nimbo_rss)

A simple to use application for fetch data from news sites 

## Getting Started

For run this project on your own local machine or server you should install mysql and java and maven.

### Prerequisites

For installing dependencies for this project do this instructions.

Install mysql

```
sudo apt install mysql-server
```

Install java
```
sudo apt install openjdk-8-jdk
```

Install maven
```
sudo apt install maven
```
### Installing

After that edit the database.properties file in src/main/resources to your mysql user and run files on db directory to initialize database

```
database.username=root

database.password=password
```

## Running the tests

The tests for database is working with h2 database and all tests will run by this command
```
mvn test
```

### Usage

For working with the application run the jar file or run App in your own ide. This application will fetch data automatically by the news rate based on each site.

Most important queries:

```
search       Search in entries
add          Add a new site to repository
exit         Save data and exit
date-report  Report for each date for each site
hour-report  Report for each hour for each site
```

Examples:

```
search --title="The title" --content="The content"
```
```
date-report --title="The title"
```

For see full documentation for app and each command use `--help` ahead of that

## Built With

* [SLF4J](https://www.slf4j.org/) - The log API used
* [JOOQ](https://www.jooq.org/) - Used to generate queries
* [JSOUP](https://jsoup.org/) - Used to fetch content of news
* [PICOCLI](https://picocli.info/) - Used to create Command Line Interface(CLI)
* [Maven](https://maven.apache.org/) - Dependency Management
* [ROME](https://rometools.github.io/rome/) - Used to parse RSS Feeds 

## Authors

* **Ehsan Karimi** - [github](https://github.com/karimiehsan90)
* **Amin Borjian** - [github](https://github.com/Borjianamin98)

See also the list of [contributors](https://github.com/karimiehsan90/nimbo_rss/graphs/contributors) who participated in this project.
