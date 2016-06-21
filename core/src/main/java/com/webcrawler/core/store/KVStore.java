package com.webcrawler.core.store;

/**
 * An interface to access key-value data structure
 */
public interface KVStore {

    interface KeyValueProcessor {
        boolean process(byte[] key, byte[] data);
    }

    interface KeyProcessor {
        boolean process(byte[] key);
    }

    void open();

    void close();

    boolean isEmpty();

    void put(byte[] key, byte[] data);

    void remove(byte[] key);

    byte[] get(byte[] key);

    void iterate(byte[] keyPrefix, KeyValueProcessor kvProcessor);

    void iterate(byte[] keyPrefix, KeyProcessor keyProcessor);
}
