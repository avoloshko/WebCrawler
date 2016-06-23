package com.webcrawler.core.loading;

import com.google.common.base.Stopwatch;
import com.google.inject.Singleton;
import com.webcrawler.core.configuration.ConfigProvider;
import com.webcrawler.core.configuration.Configuration;
import com.webcrawler.core.index.IndexPageInfo;
import com.webcrawler.core.net.Connection;
import com.webcrawler.core.net.Response;
import com.webcrawler.core.parse.ParserResult;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.*;

/**
 * The class crawls pages concurrently
 */
@Singleton
public class MultiThreadPageLoader extends ObservablePageLoader {

    private final static Logger logger = LoggerFactory.getLogger(MultiThreadPageLoader.class);

    private final ThreadPoolExecutor threadPoolExecutor;

    @Inject
    public MultiThreadPageLoader(ConfigProvider configReader) {

        Configuration.Loader loaderConfig = configReader.getConfig().getLoader();
        int maxThreads = loaderConfig.getMaxThreads();

        threadPoolExecutor = new ThreadPoolExecutor(maxThreads == 0 ? 0 : maxThreads,
                maxThreads == 0 ? Integer.MAX_VALUE : maxThreads,
                60L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    private final Set<URI> successSet = new ConcurrentHashSet<>();
    private final Set<URI> errorSet = new ConcurrentHashSet<>();
    private final Set<URI> requestedSet = new ConcurrentHashSet<>();

    @Override
    public void add(final URI startURI) {
        processURI(startURI);
    }

    @Override
    public boolean completed() {
        return successSet.size()
                + errorSet.size() == requestedSet.size();
    }

    @Override
    public void cancel() {
        successSet.clear();
        errorSet.clear();
        requestedSet.clear();

        threadPoolExecutor.getQueue().clear();
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) {

        Callable<?> callable = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        };

        try {
            int activeCount = 1;
            while (activeCount != 0) {
                final Future<?> future = threadPoolExecutor.submit(callable);
                future.get(timeout, unit);
                activeCount = threadPoolExecutor.getActiveCount();
            }
            return true;
        } catch (TimeoutException ex) {
            return false;
        } catch (Exception ex) {
            return true;
        }
    }

    @Override
    public void terminate() {
        super.terminate();

        cancel();
        threadPoolExecutor.shutdownNow();
    }

    private void processURI(final URI uri) {
        requestedSet.add(uri);
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (!requestedSet.contains(uri)) {
                    // was canceled
                    return;
                }

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
                    final Stopwatch stopwatch = Stopwatch.createStarted();

                    final Response httpResponse;
                    try {
                        httpResponse = connection.get();
                    } catch (Exception ex) {
                        if (!requestedSet.contains(uri)) {
                            // was canceled
                            return;
                        }

                        errorSet.add(uri);
                        logger.trace("Error occurred on the page: " + uri);
                        notifyFailure(uri);
                        notifyCompleted(successSet.size()
                                + errorSet.size(), requestedSet.size());
                        return;
                    }

                    loadTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);

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

                if (!requestedSet.contains(uri)) {
                    // was canceled
                    return;
                }

                successSet.add(uri);

                for (URI internalURI : indexPageInfo.getInternalURIs()) {
                    if (requestedSet.contains(internalURI)) {
                        continue;
                    }

                    processURI(internalURI);
                }

                if (!notModified) {
                    // update index
                    indexWriter.updatePageInfo(uri, indexPageInfo);
                }

                notifyPageProcessed(createPageInfo(indexPageInfo, uri, notModified, loadTime));

                notifyCompleted(successSet.size()
                        + errorSet.size(), requestedSet.size());
            }
        });
    }
}