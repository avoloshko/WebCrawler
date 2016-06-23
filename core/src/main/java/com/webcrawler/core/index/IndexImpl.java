package com.webcrawler.core.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webcrawler.core.store.KVStore;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The class implements index to store information about web pages
 */
@Singleton
public class IndexImpl implements IndexModifier, Index {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    private KVStore kvStore;

    @Override
    public void init() {
        kvStore.open();
    }

    @Override
    public void terminate() {
        kvStore.close();
    }

    @Override
    public void updatePageInfo(URI uri, IndexPageInfo page) {
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(page);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

        kvStore.put(uri.toString(), bytes);
    }

    @Override
    public Collection<URI> getSiteURIs(URI uri) {
        final List<URI> urls = new ArrayList<>();
        kvStore.iterate(uri.toString(), new KVStore.KeyProcessor() {
            @Override
            public boolean process(String key) {
                urls.add(URI.create(key));
                return false;
            }
        });
        return urls;
    }

    @Override
    public IndexPageInfo getPageInfo(URI uri) {
        byte[] bytes = kvStore.get(uri.toString());
        if (bytes == null) {
            return null;
        }

        try {
            return objectMapper.readValue(bytes, IndexPageInfo.class);
        } catch (IOException e) {
            return null;
        }
    }
}
