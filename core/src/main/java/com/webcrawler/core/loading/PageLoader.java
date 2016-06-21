package com.webcrawler.core.loading;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Page loader interface. It allows to add multiple pages to scan, cancel and terminate scan process.
 */
public interface PageLoader {

    /**
     * Adds a page to scan. Many pages can be added
     */
    void add(URI startURI);

    /**
     * Cancels scan process. Keeps all allocated resources
     */
    void cancel();

    /**
     * Returns true if there are no pages to scan
     */
    boolean completed();

    /**
     * Blocks the current thread until scan process in progress.
     * False is returned if wait time is elapsed but there are still pages to scan
     */
    boolean await(long timeout, TimeUnit unit);

    /**
     * Terminates scan process and frees all allocated resources
     */
    void terminate();
}