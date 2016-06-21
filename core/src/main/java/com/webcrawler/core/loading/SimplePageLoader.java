package com.webcrawler.core.loading;

import com.google.common.base.Stopwatch;
import com.google.inject.Singleton;
import com.webcrawler.core.index.IndexPageInfo;
import com.webcrawler.core.net.Connection;
import com.webcrawler.core.net.Response;
import com.webcrawler.core.parse.ParserResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The class crawls pages sequentially. add() method blocks the current thread.
 */
@Singleton
public class SimplePageLoader extends ObservablePageLoader {

    private final static Logger logger = LoggerFactory.getLogger(SimplePageLoader.class);

    @Override
    public void add(final URI startURI) {

        final Set<URI> successSet = new HashSet<>();
        final Set<URI> errorSet = new HashSet<>();
        final Set<URI> queue = new LinkedHashSet<>();
        queue.add(startURI);

        final Stopwatch stopwatch = Stopwatch.createUnstarted();

        while (queue.size() > 0) {
            Iterator<URI> it = queue.iterator();
            URI uri = it.next();
            it.remove();

            logger.trace("Loading page: " + uri);

            boolean notModified = false;
            IndexPageInfo indexPageInfo = indexReader.getPageInfo(uri);
            final long now = System.currentTimeMillis();
            long loadTime = 0;
            if (indexPageInfo != null
                    && (now - indexPageInfo.getTimeLoaded()) < INDEX_UPDATE_INTERVAL_MILLIS) {
                // the page has been recently processed
                notModified = true;
            } else {
                Connection connection = connectionFactory.createConnection(uri);
                if (indexPageInfo != null) {
                    connection.setModifiedSince(indexPageInfo.getTimeLastModified());
                }

                stopwatch.start();

                Response httpResponse;
                try {
                    httpResponse = connection.get();
                } catch (Exception ex) {
                    errorSet.add(uri);
                    logger.warn("Error occurred on the page: " + uri);
                    notifyFailure(uri);
                    notifyCompleted(successSet.size()
                            + errorSet.size(), successSet.size() + errorSet.size() + queue.size());
                    continue;
                }

                loadTime = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);

                if (httpResponse.isNotModified()) {
                    notModified = true;
                } else {
                    ParserResult result = parser.parse(uri, httpResponse.getData());

                    indexPageInfo = new IndexPageInfo();
                    indexPageInfo.setExternalURIs(result.getExternalURIs());
                    indexPageInfo.setImages(result.getImages());
                    indexPageInfo.setInternalURIs(result.getInternalURIs());
                    indexPageInfo.setTimeLastModified(httpResponse.getDateLastModified());
                    indexPageInfo.setTimeLoaded(now);
                    indexPageInfo.setPageSize(httpResponse.getData().length);
                    indexPageInfo.setTitle(result.getTitle());
                }
            }

            successSet.add(uri);

            assert indexPageInfo != null;
            for (URI internalURI : indexPageInfo.getInternalURIs()) {
                if (successSet.contains(internalURI)
                        || errorSet.contains(internalURI)) {
                    continue;
                }

                queue.add(internalURI);
            }

            if (!notModified) {
                // update index
                indexWriter.updatePageInfo(uri, indexPageInfo);
            }

            notifyPageProcessed(createPageInfo(indexPageInfo, uri, notModified, loadTime));

            notifyCompleted(successSet.size()
                    + errorSet.size(), successSet.size() + errorSet.size() + queue.size());
        }
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean completed() {
        return true;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) {
        return true;
    }
}