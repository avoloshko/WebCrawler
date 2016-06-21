# WebCrawler
WebCrawler is a simple Java based framework which scans websites concurrently and stores indexed data into persistent storage.

## Building and running

### Prerequisites
Version numbers below indicate the versions used.

 * Maven 3.3.9 (http://maven.apache.org)
 * Java 1.8.0_73 (http://java.oracle.com)

### Building Steps

 * ```git clone https://github.com/avoloshko/WebCrawler```
 * ```cd WebCrawler```
 * ```mvn clean package```

The most important results of the build are
 * _web/target/web-\<version\>.jar_ - A console application 
 * _cli/target/cli-\<version\>.jar_ - A web application

## Running

 * ```java -jar cli/target/cli-<version>.jar crawl cli/dist/configuration.yml --href https://google.com```
 * ```java -jar web/target/web-<version>.jar server web/dist/configuration.yml```

By default the web application uses port 8080.

### Working files

Database is saved into the webcrawler.db directory if LevelDB is configured for storage. (Memory data store is never persisted.) If you want a clean start then remove the entire data directory.

## Documentation
 * [Architecture](docs/architecture.md)
 * [Configuration](docs/configuration.md)
