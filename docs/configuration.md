# Configuration
Configuration goes under the ```webCrawler``` section of a yml file. Within that you can configure various features of the application listed below.

## Web application configuration
cli/dist/configuration.yml
```
server:
    type: simple
    applicationContextPath: /
    rootPath: '/api/*'
logging:
  level: INFO
  appenders:
    - type: console
      threshold: TRACE
webCrawler:
    store:
      memory: false
      levelDB:
        database: webcrawler.db
        cacheSize: 104857600
    loader:
        maxThreads: 10
        resourceTimeout: 5000
```

## Cli application configuration
cli/dist/configuration.yml
```
logging:
  level: INFO
  appenders:
    - type: console
      threshold: TRACE
webCrawler:
    store:
      memory: false
      levelDB:
        database: webcrawler.db
        cacheSize: 104857600
    loader:
        maxThreads: 10
        resourceTimeout: 5000
```

## Explaining the config options
### store.memory
Set true to keep index in memory

### levelDB.database
Relative or absolute path to store LevelDB data

### levelDB.cacheSize
LevelDB in-memory cache size

### loader.maxThreads
Max threads to load / process pages concurrently

### loader.resourceTimeout
Max timeout in MS to wait before timing out
