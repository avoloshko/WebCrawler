package com.webcrawler.core;

import com.webcrawler.core.store.KVStore;
import com.webcrawler.core.store.MemoryStore;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MemoryStoreTest {

    private MemoryStore store;

    @Before
    public void setUp() {
        store = new MemoryStore();
    }

    @Test
    public void testGetPutRemove() throws IOException {

        String value = "v12345", key = "k12345";

        assertNull(store.get(value.getBytes()));

        store.put(key.getBytes(), value.getBytes());

        assertArrayEquals(store.get(key.getBytes()), value.getBytes());

        store.remove(store.get(key.getBytes()));

        assertNull(store.get(value.getBytes()));
    }

    @Test
    public void testIsEmpty() throws IOException {

        String value = "v12345", key = "k12345";

        assertTrue(store.isEmpty());

        store.put(key.getBytes(), value.getBytes());

        assertFalse(store.isEmpty());
    }

    @Test
    public void testIterateKeys() throws IOException {

        store.put("k12345".getBytes(), "v12345".getBytes());
        store.put("k23456".getBytes(), "v23456".getBytes());

        final List<String> keys = new ArrayList<>();
        store.iterate("k1".getBytes(), new KVStore.KeyProcessor() {
            @Override
            public boolean process(byte[] key) {
                keys.add(new String(key));
                return false;
            }
        });

        assertTrue(keys.size() == 1);
        assertEquals(keys.get(0), "k12345");

        keys.clear();
        store.iterate("k".getBytes(), new KVStore.KeyProcessor() {
            @Override
            public boolean process(byte[] key) {
                keys.add(new String(key));
                return false;
            }
        });

        assertTrue(keys.size() == 2);
        assertTrue(keys.contains("k12345"));
        assertTrue(keys.contains("k23456"));
    }

    @Test
    public void testIterateKeysValues() throws IOException {

        store.put("k12345".getBytes(), "v12345".getBytes());
        store.put("k23456".getBytes(), "v23456".getBytes());

        final List<String> keys = new ArrayList<>();
        final List<String> values = new ArrayList<>();
        store.iterate("k1".getBytes(), new KVStore.KeyValueProcessor() {
            @Override
            public boolean process(byte[] key, byte[] value) {
                keys.add(new String(key));
                values.add(new String(value));
                return false;
            }
        });

        assertTrue(keys.size() == 1);
        assertEquals(keys.get(0), "k12345");
        assertEquals(values.get(0), "v12345");

        keys.clear();
        values.clear();
        store.iterate("k".getBytes(), new KVStore.KeyValueProcessor() {
            @Override
            public boolean process(byte[] key, byte[] value) {
                keys.add(new String(key));
                values.add(new String(value));
                return false;
            }
        });

        assertTrue(keys.size() == 2);
        assertTrue(keys.contains("k12345"));
        assertTrue(keys.contains("k23456"));
    }
}
