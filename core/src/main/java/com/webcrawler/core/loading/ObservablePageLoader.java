package com.webcrawler.core.loading;

import com.webcrawler.core.PageInfo;
import com.webcrawler.core.WebCrawlerObservable;
import com.webcrawler.core.index.IndexPageInfo;
import com.webcrawler.core.index.Index;
import com.webcrawler.core.index.IndexModifier;
import com.webcrawler.core.net.ConnectionFactory;
import com.webcrawler.core.parse.Parser;
import org.eclipse.jetty.util.ConcurrentHashSet;

import javax.inject.Inject;
import java.net.URI;
import java.util.Set;

/**
 * Base class for page loaders
 */
public abstract class ObservablePageLoader implements WebCrawlerObservable, PageLoader {

    static final int INDEX_UPDATE_INTERVAL_MILLIS = 300 * 1000;

    @Inject
    IndexModifier indexWriter;

    @Inject
    Index indexReader;

    @Inject
    Parser parser;

    @Inject
    ConnectionFactory connectionFactory;

    private final Set<CompleteObserver> completeObservers = new ConcurrentHashSet<>();
    private final Set<FailureObserver> failureObservers = new ConcurrentHashSet<>();
    private final Set<ProgressObserver> progressObservers = new ConcurrentHashSet<>();

    @Override
    public void addObserver(CompleteObserver observer) {
        if (observer == null) {
            throw new NullPointerException();
        }
        completeObservers.add(observer);
    }

    @Override
    public void removeObserver(CompleteObserver observer) {
        completeObservers.remove(observer);
    }

    @Override
    public void addObserver(FailureObserver observer) {
        if (observer == null) {
            throw new NullPointerException();
        }
        failureObservers.add(observer);
    }

    @Override
    public void removeObserver(FailureObserver observer) {
        failureObservers.remove(observer);
    }

    @Override
    public void addObserver(ProgressObserver observer) {
        if (observer == null) {
            throw new NullPointerException();
        }
        progressObservers.add(observer);
    }

    @Override
    public void removeObserver(ProgressObserver observer) {
        progressObservers.remove(observer);
    }

    public void removeObservers() {
        completeObservers.clear();
        failureObservers.clear();
        progressObservers.clear();
    }

    void notifyFailure(URI uri) {
        for (FailureObserver failureObserver : failureObservers) {
            failureObserver.onFailed(uri);
        }
    }

    void notifyPageProcessed(PageInfo pageContext) {
        for (ProgressObserver progressObserver : progressObservers) {
            progressObserver.onPageProcessed(pageContext);
        }
    }

    void notifyCompleted(int processedCount, int totalCount) {
        for (CompleteObserver completeObserver : completeObservers) {
            completeObserver.onCompleted(processedCount, totalCount);
        }
    }

    @Override
    public void terminate() {
    }

    static PageInfo createPageInfo(IndexPageInfo indexPageInfo, URI uri, boolean notModified, long loadTime) {
        PageInfo pageInfo = new PageInfo();
        pageInfo.setTitle(indexPageInfo.getTitle());
        pageInfo.setPageSize(indexPageInfo.getPageSize());
        pageInfo.setInternalURIs(indexPageInfo.getInternalURIs());
        pageInfo.setExternalURIs(indexPageInfo.getExternalURIs());
        pageInfo.setImages(indexPageInfo.getImages());
        pageInfo.setPageSize(indexPageInfo.getPageSize());
        pageInfo.setUri(uri);
        pageInfo.setNotModified(notModified);
        pageInfo.setPageLoadTime(loadTime);

        return pageInfo;
    }
}