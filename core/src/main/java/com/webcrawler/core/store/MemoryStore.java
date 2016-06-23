package com.webcrawler.core.store;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A KVStore which stores key-value data in memory. It is thread-safe
 */
@Singleton
public class MemoryStore implements KVStore {

    private Map<String, byte[]> hashMap = new ConcurrentHashMap<>();

    public void open() {

    }

    public void close() {
    }

    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    public void put(String key, byte[] data) {
        hashMap.put(key, data);
    }

    public void remove(String key) {
        hashMap.remove(key);
    }

    public byte[] get(String key) {
        return hashMap.get(key);
    }

    @Override
    public void iterate(String keyPrefix, KeyValueProcessor kvProcessor) {

        for (Map.Entry<String, byte[]> entry : hashMap.entrySet()) {
            if (entry.getKey().startsWith(keyPrefix)) {
                if (kvProcessor.process(entry.getKey(), entry.getValue())) {
                    break;
                }
            }
        }
    }

    @Override
    public void iterate(String keyPrefix, KeyProcessor keyProcessor) {

        for (String key : hashMap.keySet()) {
            if (key.startsWith(keyPrefix)) {
                if (keyProcessor.process(key)) {
                    break;
                }
            }
        }
    }
}