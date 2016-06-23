package com.webcrawler.core.store;

/**
 * An interface to access key-value data structure
 */
public interface KVStore {

    interface KeyValueProcessor {
        boolean process(String key, byte[] data);
    }

    interface KeyProcessor {
        boolean process(String key);
    }

    void open();

    void close();

    boolean isEmpty();

    void put(String key, byte[] data);

    void remove(String key);

    byte[] get(String key);

    void iterate(String keyPrefix, KeyValueProcessor kvProcessor);

    void iterate(String keyPrefix, KeyProcessor keyProcessor);
}
