package com.webcrawler.core;

import java.net.URI;

/**
 * Observable interface to track crawling process
 */
public interface WebCrawlerObservable {

    interface CompleteObserver {
        void onCompleted(int completedCount, int totalCount);
    }

    interface FailureObserver {
        void onFailed(URI uri);
    }

    interface ProgressObserver {
        void onPageProcessed(PageInfo pageContext);
    }

    void addObserver(CompleteObserver observer);
    void removeObserver(CompleteObserver observer);

    void addObserver(FailureObserver observer);
    void removeObserver(FailureObserver observer);

    void addObserver(ProgressObserver observer);
    void removeObserver(ProgressObserver observer);
}
