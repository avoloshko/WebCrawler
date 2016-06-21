The primary goal of the application is to scan websites and retrieve the most important information from them. The application is easy-to-use and has just a few prerequisites. The core part can be utilized independently. It uses abstraction layers to separate code into parts and provide greater extensibility.

The application holds an index in either memory or LevelDB local database. LevelDB is a fast and convenient embedded data storage. It can not be replicated and there is no MapReduce feature. Nevertheless, the core part allows to add more advanced database backends and use them together with other layers.

The Web and CLI applications allow to scan websites and see the results interactively.

### Known issues / limitations
 * The same LevelDB database folder can not be used by multiple processes. Currently, there is no way to set up a cluster which shares the same index, but this can be fixed by integrating a more efficient database backend which supports replication
 * robots.txt files are not considered
 * The web crawler omits URL query parameters
 * ETag and Expires headers are not supported. Last-Modified header is supported
 * DNS resolution is not supported yet
