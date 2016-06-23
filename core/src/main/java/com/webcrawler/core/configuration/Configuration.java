package com.webcrawler.core.configuration;

/**
 * The class holds configuration
 */
public class Configuration {

    private Store store;

    private Loader loader;

    public static class Loader {

        /**
         * Max threads to load / process pages concurrently
         */
        private int maxThreads;

        /**
         * Max timeout in MS to wait response from Connection
         */
        private int resourceTimeout;

        public Loader(int maxThreads, int resourceTimeout) {
            this.maxThreads = maxThreads;
            this.resourceTimeout = resourceTimeout;
        }

        public Loader() {
        }

        public int getMaxThreads() {
            return maxThreads;
        }

        public int getResourceTimeout() {
            return resourceTimeout;
        }
    }

    public static class LevelDB {

        /**
         * Relative or absolute path to store LevelDB data
         */
        private String database;

        /**
         * LevelDB in-memory cache size
         */
        private int cacheSize;

        public LevelDB() {
        }

        public LevelDB(String database, int cacheSize) {
            this.database = database;
            this.cacheSize = cacheSize;
        }

        public String getDatabase() {
            return database;
        }

        public int getCacheSize() {
            return cacheSize;
        }
    }

    static class Store {

        /**
         * True to use memory-based KVStore. False to use LevelDB KVStore
         */
        private boolean memory;

        private LevelDB levelDB;

        public Store() {
        }

        public Store(boolean memory, LevelDB levelDB) {
            this.memory = memory;
            this.levelDB = levelDB;
        }

        public LevelDB getLevelDB() {
            return levelDB;
        }

        public boolean useMemory() {
            return memory;
        }
    }

    public Configuration(Store store, Loader loader) {
        this.store = store;
        this.loader = loader;
    }

    private Configuration() {
    }

    public Store getStore() {
        return store;
    }

    public Loader getLoader() {
        return loader;
    }
}
