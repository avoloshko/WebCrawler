package com.webcrawler.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webcrawler.core.configuration.ConfigProvider;
import com.webcrawler.core.configuration.Configuration;
import com.webcrawler.core.index.IndexModifier;
import com.webcrawler.core.index.IndexPageInfo;
import com.webcrawler.core.loading.MultiThreadPageLoader;
import com.webcrawler.core.net.Connection;
import com.webcrawler.core.net.ConnectionFactory;
import com.webcrawler.core.net.Response;
import com.webcrawler.core.parse.Parser;
import com.webcrawler.core.parse.ParserResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MultiThreadPageLoaderTest {

    @Mock
    IndexModifier indexReader;

    @Mock
    IndexModifier indexWriter;

    @Mock
    Parser parser;

    @Mock
    ConnectionFactory connectionFactory;

    @Mock
    ConfigProvider configReader;

    @InjectMocks
    private MultiThreadPageLoader pageLoader = new MultiThreadPageLoader(new ConfigProvider() {
        @Override
        public Configuration getConfig() {
            return new Configuration(null, new Configuration.Loader(10, 5000));
        }
    });

    @Test
    public void testNotModified() throws MalformedURLException, JsonProcessingException {
        URI pageURI = URI.create("https://webcrawler.app");

        final int[] completed = { 0, 0 };
        final List<PageInfo> processedPages = new ArrayList<>();
        final List<URI> failedPages = new ArrayList<>();

        List<URI> externalURIs = Arrays.asList(URI.create("https://webcrawler.app/page1/subpage1"));
        List<URI> internalURIs = Arrays.asList(pageURI);
        List<URI> images = Arrays.asList(URI.create("https://webcrawler.app/page1"));
        String title = "About WebCrawler";
        long timeLastModified = 103;
        long timeLoaded = System.currentTimeMillis();
        int pageSize = 123;

        IndexPageInfo indexPageInfo = new IndexPageInfo();
        indexPageInfo.setInternalURIs(internalURIs);
        indexPageInfo.setExternalURIs(externalURIs);
        indexPageInfo.setImages(images);
        indexPageInfo.setTitle(title);
        indexPageInfo.setPageSize(pageSize);
        indexPageInfo.setTimeLoaded(timeLoaded);
        indexPageInfo.setTimeLastModified(timeLastModified);

        // setupMock
        when(indexReader.getPageInfo(pageURI)).thenReturn(indexPageInfo);

        // test
        pageLoader.addObserver(new WebCrawlerObservable.ProgressObserver() {
            @Override
            public void onPageProcessed(PageInfo pageContext) {
                processedPages.add(pageContext);
            }
        });
        pageLoader.addObserver(new WebCrawlerObservable.FailureObserver() {
            @Override
            public void onFailed(URI uri) {
                failedPages.add(uri);
            }
        });
        pageLoader.addObserver(new WebCrawlerObservable.CompleteObserver() {
            @Override
            public void onCompleted(int completedCount, int totalCount) {
                completed[0] = completedCount;
                completed[1] = totalCount;
            }
        });
        pageLoader.add(pageURI);
        pageLoader.await(10, TimeUnit.SECONDS);

        assertEquals(failedPages.size(), 0);
        assertEquals(completed[0], 1);
        assertEquals(completed[1], 1);
        assertEquals(processedPages.size(), 1);
        assertEquals(processedPages.get(0).isNotModified(), true);
        assertArrayEquals(processedPages.get(0).getImages().toArray(), images.toArray());
        assertArrayEquals(processedPages.get(0).getExternalURIs().toArray(), externalURIs.toArray());
        assertEquals(processedPages.get(0).getTitle(), title);
        assertArrayEquals(processedPages.get(0).getInternalURIs().toArray(), internalURIs.toArray());
        assertEquals(processedPages.get(0).getPageLoadTime(), 0);
        assertEquals(processedPages.get(0).getPageSize(), pageSize);
        assertEquals(processedPages.get(0).getUri(), pageURI);
    }

    @Test
    public void testPageIsNotModified() throws MalformedURLException, JsonProcessingException {
        URI pageURI = URI.create("https://webcrawler.app");

        final int[] completed = { 0, 0 };
        final List<PageInfo> processedPages = new ArrayList<>();
        final List<URI> failedPages = new ArrayList<>();

        List<URI> externalURIs = Arrays.asList(URI.create("https://webcrawler.app/page1/subpage1"));
        List<URI> internalURIs = Arrays.asList(pageURI);
        List<URI> images = Arrays.asList(URI.create("https://webcrawler.app/page1"));
        String title = "About WebCrawler";
        final long timeLastModified = 103;
        long timeLoaded = System.currentTimeMillis();
        int pageSize = 123;

        IndexPageInfo indexPageInfo = new IndexPageInfo();
        indexPageInfo.setInternalURIs(internalURIs);
        indexPageInfo.setExternalURIs(externalURIs);
        indexPageInfo.setImages(images);
        indexPageInfo.setTitle(title);
        indexPageInfo.setPageSize(pageSize);
        indexPageInfo.setTimeLoaded(timeLoaded);
        indexPageInfo.setTimeLastModified(timeLastModified);

        // setupMock
        when(indexReader.getPageInfo(pageURI)).thenReturn(indexPageInfo);
        when(connectionFactory.createConnection(pageURI)).thenReturn(new Connection() {
            long ifModifiedSince;
            @Override
            public void setModifiedSince(long ifModifiedSince) {
                this.ifModifiedSince = ifModifiedSince;
            }

            @Override
            public Response get() throws IOException {
                if (ifModifiedSince == timeLastModified) {
                    return new Response(true, null, 0, true);
                }
                throw new IOException();
            }
        });

        // test
        pageLoader.addObserver(new WebCrawlerObservable.ProgressObserver() {
            @Override
            public void onPageProcessed(PageInfo pageContext) {
                processedPages.add(pageContext);
            }
        });
        pageLoader.addObserver(new WebCrawlerObservable.FailureObserver() {
            @Override
            public void onFailed(URI uri) {
                failedPages.add(uri);
            }
        });
        pageLoader.addObserver(new WebCrawlerObservable.CompleteObserver() {
            @Override
            public void onCompleted(int completedCount, int totalCount) {
                completed[0] = completedCount;
                completed[1] = totalCount;
            }
        });
        pageLoader.add(pageURI);
        pageLoader.await(10, TimeUnit.SECONDS);

        assertEquals(failedPages.size(), 0);
        assertEquals(completed[0], 1);
        assertEquals(completed[1], 1);
        assertEquals(processedPages.size(), 1);
        assertEquals(processedPages.get(0).isNotModified(), true);
        assertArrayEquals(processedPages.get(0).getImages().toArray(), images.toArray());
        assertArrayEquals(processedPages.get(0).getExternalURIs().toArray(), externalURIs.toArray());
        assertEquals(processedPages.get(0).getTitle(), title);
        assertArrayEquals(processedPages.get(0).getInternalURIs().toArray(), internalURIs.toArray());
        assertEquals(processedPages.get(0).getPageLoadTime(), 0);
        assertEquals(processedPages.get(0).getPageSize(), pageSize);
        assertEquals(processedPages.get(0).getUri(), pageURI);
    }

    @Test
    public void testErrorOccurred() throws MalformedURLException, JsonProcessingException {
        URI pageURI = URI.create("https://webcrawler.app");

        final int[] completed = { 0, 0 };
        final List<PageInfo> processedPages = new ArrayList<>();
        final List<URI> failedPages = new ArrayList<>();

        // setupMock
        when(indexReader.getPageInfo(pageURI)).thenReturn(null);
        when(connectionFactory.createConnection(pageURI)).thenReturn(new Connection() {
            @Override
            public void setModifiedSince(long ifModifiedSince) {

            }

            @Override
            public Response get() throws IOException {
                throw new IOException();
            }
        });

        // test
        pageLoader.addObserver(new WebCrawlerObservable.ProgressObserver() {
            @Override
            public void onPageProcessed(PageInfo pageContext) {
                processedPages.add(pageContext);
            }
        });
        pageLoader.addObserver(new WebCrawlerObservable.FailureObserver() {
            @Override
            public void onFailed(URI uri) {
                failedPages.add(uri);
            }
        });
        pageLoader.addObserver(new WebCrawlerObservable.CompleteObserver() {
            @Override
            public void onCompleted(int completedCount, int totalCount) {
                completed[0] = completedCount;
                completed[1] = totalCount;
            }
        });
        pageLoader.add(pageURI);
        pageLoader.await(10, TimeUnit.SECONDS);

        assertEquals(failedPages.size(), 1);
        assertEquals(failedPages.get(0), pageURI);
        assertEquals(completed[0], 1);
        assertEquals(completed[1], 1);
        assertEquals(processedPages.size(), 0);
    }

    @Test
    public void testNormal() throws MalformedURLException, JsonProcessingException {
        final URI pageURI = URI.create("https://webcrawler.app");

        final int[] completed = { 0, 0 };
        final List<PageInfo> processedPages = new ArrayList<>();
        final List<URI> failedPages = new ArrayList<>();

        final String content = "page content";

        final List<URI> externalURIs = Arrays.asList(URI.create("https://webcrawler.app/page1/subpage1"));
        final List<URI> internalURIs = Arrays.asList(pageURI);
        final List<URI> images = Arrays.asList(URI.create("https://webcrawler.app/page1"));
        final String title = "About WebCrawler";
        final long timeLastModified = 103;
        long timeLoaded = 104;
        int pageSize = 123;

        IndexPageInfo indexPageInfo = new IndexPageInfo();
        indexPageInfo.setInternalURIs(internalURIs);
        indexPageInfo.setExternalURIs(externalURIs);
        indexPageInfo.setImages(images);
        indexPageInfo.setTitle(title);
        indexPageInfo.setPageSize(pageSize);
        indexPageInfo.setTimeLoaded(timeLoaded);
        indexPageInfo.setTimeLastModified(timeLastModified);

        ParserResult parserResult = new ParserResult();
        parserResult.setInternalURIs(internalURIs);
        parserResult.setExternalURIs(externalURIs);
        parserResult.setImages(images);
        parserResult.setTitle(title);

        // setupMock
        when(indexReader.getPageInfo(pageURI)).thenReturn(null);
        when(connectionFactory.createConnection(pageURI)).thenReturn(new Connection() {
            @Override
            public void setModifiedSince(long ifModifiedSince) {
                fail();
            }

            @Override
            public Response get() throws IOException {
                return new Response(true, content.getBytes(), 0, false);
            }
        });
        when(parser.parse(pageURI, content.getBytes())).thenReturn(parserResult);
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock param) throws Throwable {
                assertEquals(param.getArgumentAt(0, URI.class), pageURI);

                assertEquals(param.getArgumentAt(1, IndexPageInfo.class).getTitle(), title);
                assertEquals(param.getArgumentAt(1, IndexPageInfo.class).getPageSize(), content.length());
                assertArrayEquals(param.getArgumentAt(1, IndexPageInfo.class).getInternalURIs().toArray(), internalURIs.toArray());
                assertArrayEquals(param.getArgumentAt(1, IndexPageInfo.class).getExternalURIs().toArray(), externalURIs.toArray());
                assertArrayEquals(param.getArgumentAt(1, IndexPageInfo.class).getImages().toArray(), images.toArray());

                return null;
            }
        }).when(indexWriter).updatePageInfo(eq(pageURI), any(IndexPageInfo.class));

        // test
        pageLoader.addObserver(new WebCrawlerObservable.ProgressObserver() {
            @Override
            public void onPageProcessed(PageInfo pageContext) {
                processedPages.add(pageContext);
            }
        });
        pageLoader.addObserver(new WebCrawlerObservable.FailureObserver() {
            @Override
            public void onFailed(URI uri) {
                failedPages.add(uri);
            }
        });
        pageLoader.addObserver(new WebCrawlerObservable.CompleteObserver() {
            @Override
            public void onCompleted(int completedCount, int totalCount) {
                completed[0] = completedCount;
                completed[1] = totalCount;
            }
        });
        pageLoader.add(pageURI);
        pageLoader.await(10, TimeUnit.SECONDS);

        assertEquals(failedPages.size(), 0);
        assertEquals(completed[0], 1);
        assertEquals(completed[1], 1);
        assertEquals(processedPages.size(), 1);
        assertEquals(processedPages.get(0).isNotModified(), false);
        assertArrayEquals(processedPages.get(0).getImages().toArray(), images.toArray());
        assertArrayEquals(processedPages.get(0).getExternalURIs().toArray(), externalURIs.toArray());
        assertEquals(processedPages.get(0).getTitle(), title);
        assertArrayEquals(processedPages.get(0).getInternalURIs().toArray(), internalURIs.toArray());
        assertEquals(processedPages.get(0).getPageSize(), content.length());
        assertEquals(processedPages.get(0).getUri(), pageURI);
    }

    @Test
    public void testNormalWithInternalURL() throws MalformedURLException, JsonProcessingException {
        final URI pageURI = URI.create("https://webcrawler.org");
        final URI internalURI = URI.create("https://webcrawler.app/page1");

        final int[] completed = { 0, 0 };
        final List<PageInfo> processedPages = new ArrayList<>();
        final List<URI> failedPages = new ArrayList<>();

        final String content = "page content";

        final List<URI> externalURIs = Arrays.asList(URI.create("https://webcrawler.app/page1/subpage1"));
        final List<URI> internalURIs = Arrays.asList(internalURI);
        final List<URI> images = Arrays.asList(URI.create("https://webcrawler.app/page1"));
        final String title = "About WebCrawler";
        final long timeLastModified = 103;
        long timeLoaded = 104;
        int pageSize = 123;

        IndexPageInfo indexPageInfo = new IndexPageInfo();
        indexPageInfo.setInternalURIs(Arrays.asList(internalURI));
        indexPageInfo.setExternalURIs(externalURIs);
        indexPageInfo.setImages(images);
        indexPageInfo.setTitle(title);
        indexPageInfo.setPageSize(pageSize);
        indexPageInfo.setTimeLoaded(timeLoaded);
        indexPageInfo.setTimeLastModified(timeLastModified);

        ParserResult parserResult = new ParserResult();
        parserResult.setInternalURIs(internalURIs);
        parserResult.setExternalURIs(externalURIs);
        parserResult.setImages(images);
        parserResult.setTitle(title);

        // setupMock
        when(indexReader.getPageInfo(pageURI)).thenReturn(null);
        when(connectionFactory.createConnection(pageURI)).thenReturn(new Connection() {
            @Override
            public void setModifiedSince(long ifModifiedSince) {
                fail();
            }

            @Override
            public Response get() throws IOException {
                return new Response(true, content.getBytes(), 0, false);
            }
        });
        when(parser.parse(pageURI, content.getBytes())).thenReturn(parserResult);
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock param) throws Throwable {
                assertEquals(param.getArgumentAt(0, URI.class), pageURI);

                assertEquals(param.getArgumentAt(1, IndexPageInfo.class).getTitle(), title);
                assertEquals(param.getArgumentAt(1, IndexPageInfo.class).getPageSize(), content.length());
                assertArrayEquals(param.getArgumentAt(1, IndexPageInfo.class).getInternalURIs().toArray(), internalURIs.toArray());
                assertArrayEquals(param.getArgumentAt(1, IndexPageInfo.class).getExternalURIs().toArray(), externalURIs.toArray());
                assertArrayEquals(param.getArgumentAt(1, IndexPageInfo.class).getImages().toArray(), images.toArray());

                return null;
            }
        }).when(indexWriter).updatePageInfo(eq(pageURI), any(IndexPageInfo.class));

        // test
        pageLoader.addObserver(new WebCrawlerObservable.ProgressObserver() {
            @Override
            public void onPageProcessed(PageInfo pageContext) {
                processedPages.add(pageContext);
            }
        });
        pageLoader.addObserver(new WebCrawlerObservable.FailureObserver() {
            @Override
            public void onFailed(URI uri) {
                failedPages.add(uri);
            }
        });
        pageLoader.addObserver(new WebCrawlerObservable.CompleteObserver() {
            @Override
            public void onCompleted(int completedCount, int totalCount) {
                completed[0] = completedCount;
                completed[1] = totalCount;
            }
        });
        pageLoader.add(pageURI);
        pageLoader.await(10, TimeUnit.SECONDS);

        assertEquals(failedPages.size(), 1);
        assertEquals(failedPages.get(0), internalURI);
        assertEquals(completed[0], 2);
        assertEquals(completed[1], 2);
        assertEquals(processedPages.size(), 1);
        assertEquals(processedPages.get(0).isNotModified(), false);
        assertArrayEquals(processedPages.get(0).getImages().toArray(), images.toArray());
        assertArrayEquals(processedPages.get(0).getExternalURIs().toArray(), externalURIs.toArray());
        assertEquals(processedPages.get(0).getTitle(), title);
        assertArrayEquals(processedPages.get(0).getInternalURIs().toArray(), internalURIs.toArray());
        assertEquals(processedPages.get(0).getPageSize(), content.length());
        assertEquals(processedPages.get(0).getUri(), pageURI);
    }
}
