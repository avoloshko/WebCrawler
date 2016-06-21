package com.webcrawler.cli.app;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webcrawler.core.WebCrawler;
import com.webcrawler.core.WebCrawlerObservable;
import com.webcrawler.core.configuration.Configuration;
import com.webcrawler.core.index.IndexPageInfo;
import com.webcrawler.core.utils.URIUtils;
import io.dropwizard.Application;
import io.dropwizard.cli.Cli;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.JarLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * Command line application which crawls web pages and prints results to standard output.
 * Example arguments:
 * crawl dist/configuration.yml --href http://google.com
 * To print help:
 * -h
 */
public class CliApp extends Application<CliApp.AppConfiguration> {

    public static void main(String[] args) throws Exception {
        new CliApp().run(args);
    }

    static class AppConfiguration extends io.dropwizard.Configuration {

        @JsonProperty("webCrawler")
        Configuration webCrawler;
    }

    private final CrawlCommand<AppConfiguration> command = new CrawlCommand<>(this);

    @Override
    public void run(final String... arguments) throws Exception {
        final Bootstrap<AppConfiguration> bootstrap = new Bootstrap<>(this);
        bootstrap.addCommand(command);
        initialize(bootstrap);

        ObjectMapper objectMapper = bootstrap.getObjectMapper();
        bootstrap.getObjectMapper()
                .setVisibilityChecker(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        final Cli cli = new Cli(new JarLocation(getClass()), bootstrap, System.out, System.err);
        if (!cli.run(arguments)) {
            System.exit(1);
        }
    }

    @Override
    public void run(AppConfiguration configuration, Environment environment) {

        final Logger logger = LoggerFactory.getLogger(CliApp.class);

        final WebCrawler webCrawler = new WebCrawler(configuration.webCrawler);

        URI baseURI = URIUtils.normalizeLink(command.getHref(), null, false);
        if (baseURI == null) {
            logger.error("Wrong URI: " + command.getHref());
            System.exit(1);
        }

        webCrawler.observable().addObserver(new WebCrawlerObservable.CompleteObserver() {
            @Override
            public void onCompleted(int completedCount, int totalCount) {
                logger.info("Pages processed: " + completedCount + " / " + totalCount);
            }
        });

        final Set<URI> failedURIs = new TreeSet<>();
        webCrawler.observable().addObserver(new WebCrawlerObservable.FailureObserver() {
            @Override
            public void onFailed(URI uri) {
                failedURIs.add(uri);
            }
        });

        webCrawler.crawl(baseURI).await(10, TimeUnit.HOURS);

        /* Build a site map */
        Collection<URI> internalURIs = webCrawler.index().getSiteURIs(URI.create(command.getHref()));
        System.out.println("Internal URIs:");
        for (URI uri : internalURIs) {
            System.out.println(uri.toString());
        }

        /* Collect images from index */
        Set<URI> images = new TreeSet<>();
        Set<URI> externalURIs = new TreeSet<>();
        for (URI internalURI : internalURIs) {
            IndexPageInfo pageInfo = webCrawler.index().getPageInfo(internalURI);
            images.addAll(pageInfo.getImages());
            externalURIs.addAll(pageInfo.getExternalURIs());
        }

        System.out.println("Images:");
        for (URI uri : images) {
            System.out.println(uri.toString());
        }

        System.out.println("External URIs:");
        for (URI uri : externalURIs) {
            System.out.println(uri.toString());
        }

        System.out.println("Failed to scan:");
        for (URI uri : failedURIs) {
            System.out.println(uri.toString());
        }

        webCrawler.terminate();
    }
}