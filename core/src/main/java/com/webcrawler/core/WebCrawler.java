package com.webcrawler.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.webcrawler.core.configuration.Configuration;
import com.webcrawler.core.configuration.InjectionModule;
import com.webcrawler.core.index.Index;
import com.webcrawler.core.index.IndexModifier;
import com.webcrawler.core.loading.ObservablePageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * The class crawls web pages. It provides possibility to scan multiple pages and domains concurrently.
 * The crawling process can be observed.
 */
public class WebCrawler {
    private static final Logger logger = LoggerFactory.getLogger(WebCrawler.class);

    private final ObservablePageLoader observablePageLoader;

    private IndexModifier indexWriter;

    public WebCrawler(Configuration configuration) {
        logger.info("Working dir: " + System.getProperty("user.dir"));

        Injector injector = Guice.createInjector(new InjectionModule(configuration));

        observablePageLoader = injector.getInstance(ObservablePageLoader.class);
        indexWriter = injector.getInstance(IndexModifier.class);
        indexWriter.init();
    }

    public WebCrawler crawl(URI url) {
        index();

        observablePageLoader.add(url);
        return this;
    }

    public WebCrawler cancel() {
        observablePageLoader.cancel();
        return this;
    }

    /**
     * Waits all active operations to complete
     */
    public boolean await(long timeout, TimeUnit unit)  {
        return observablePageLoader.await(timeout, unit);
    }

    /**
     * Terminates all operations and frees allocated resources.
     * Afterwards, WebCrawler can't be used.
     */
    public void terminate()  {
        observablePageLoader.terminate();
        indexWriter.terminate();
    }

    public WebCrawlerObservable observable() {
        return observablePageLoader;
    }

    /**
     * Provides read access to indexed data
     */
    public Index index() {
        return indexWriter;
    }
}
