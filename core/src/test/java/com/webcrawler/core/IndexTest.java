package com.webcrawler.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webcrawler.core.index.IndexImpl;
import com.webcrawler.core.index.IndexPageInfo;
import com.webcrawler.core.store.KVStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IndexTest {

    @Mock
    private KVStore kvStore;

    @InjectMocks
    private IndexImpl indexImpl;

    boolean kvStoreOpened;

    boolean kvStoreClosed;

    boolean kvStoreUpdated;

    IndexPageInfo indexPageInfo;

    URI indexPageKey;

    static final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setupMock() throws MalformedURLException, JsonProcessingException {

        indexPageInfo = new IndexPageInfo();
        indexPageInfo.setExternalURIs(Arrays.asList(URI.create("https://webcrawler.app/page1/subpage1")));
        indexPageInfo.setInternalURIs(Arrays.asList(URI.create("https://webcrawler.app")));
        indexPageInfo.setImages(Arrays.asList(URI.create("https://webcrawler.app/page1")));
        indexPageInfo.setTitle("About WebCrawler");
        indexPageInfo.setPageSize(123);
        indexPageInfo.setTimeLoaded(1001);
        indexPageInfo.setTimeLastModified(1002);

        indexPageKey = URI.create(UUID.randomUUID().toString());

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock var1) throws Throwable {
                kvStoreOpened = true;
                return null;
            }
        }).when(kvStore).open();

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock var1) throws Throwable {
                kvStoreClosed = true;
                return null;
            }
        }).when(kvStore).close();

        when(kvStore.get(indexPageKey.toString())).thenReturn(objectMapper.writeValueAsBytes(indexPageInfo));

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock var1) throws Throwable {
                kvStoreUpdated = true;
                return null;
            }
        }).when(kvStore).put(indexPageKey.toString(), objectMapper.writeValueAsBytes(indexPageInfo));
    }

    @Test
    public void testInitAndTermination() {
        assertEquals(kvStoreOpened, false);
        assertEquals(kvStoreClosed, false);

        indexImpl.init();
        assertEquals(kvStoreOpened, true);

        indexImpl.terminate();
        assertEquals(kvStoreClosed, true);
    }

    @Test
    public void testUpdatePageInfo() {
        assertEquals(kvStoreUpdated, false);

        indexImpl.updatePageInfo(indexPageKey, indexPageInfo);

        assertEquals(kvStoreUpdated, true);
    }

    @Test
    public void getGetPageInfo() throws JsonProcessingException {
        IndexPageInfo pageInfo = indexImpl.getPageInfo(indexPageKey);

        assertEquals(objectMapper.writeValueAsString(indexPageInfo), objectMapper.writeValueAsString(pageInfo));
    }
}
