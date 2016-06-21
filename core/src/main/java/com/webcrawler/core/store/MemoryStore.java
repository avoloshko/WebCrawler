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

    public void put(byte[] key, byte[] data) {
        hashMap.put(new String(key), data);
    }

    public void remove(byte[] key) {
        hashMap.remove(new String(key));
    }

    public byte[] get(byte[] key) {
        return hashMap.get(new String(key));
    }

    @Override
    public void iterate(byte[] keyPrefix, KeyValueProcessor kvProcessor) {
        String sPref = new String(keyPrefix);

        for (Map.Entry<String, byte[]> entry : hashMap.entrySet()) {
            if (entry.getKey().startsWith(sPref)) {
                if (kvProcessor.process(entry.getKey().getBytes(), entry.getValue())) {
                    break;
                }
            }
        }
    }

    @Override
    public void iterate(byte[] keyPrefix, KeyProcessor keyProcessor) {
        String sPref = new String(keyPrefix);

        for (String key : hashMap.keySet()) {
            if (key.startsWith(sPref)) {
                if (keyProcessor.process(key.getBytes())) {
                    break;
                }
            }
        }
    }
}