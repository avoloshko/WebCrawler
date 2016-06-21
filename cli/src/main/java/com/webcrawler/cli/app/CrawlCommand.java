package com.webcrawler.cli.app;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements 'crawl' command
 */
public class CrawlCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private static final Logger logger = LoggerFactory.getLogger(CrawlCommand.class);

    private final Application<T> application;
    private final Class<T> configurationClass;

    private String href;

    public String getHref() {
        return href;
    }

    public CrawlCommand(Application<T> application) {
        super("crawl", "Crawls a website");
        this.application = application;
        this.configurationClass = application.getConfigurationClass();
    }

    /*
     * Since we don't subclass ServerCommand, we need a concrete reference to the configuration
     * class.
     */
    @Override
    protected Class<T> getConfigurationClass() {
        return configurationClass;
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-hr", "--href")
                .dest("href")
                .type(String.class)
                .required(true)
                .help("Sets initial URI");
    }

    @Override
    protected final void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        final Environment environment = new Environment(bootstrap.getApplication().getName(),
                bootstrap.getObjectMapper(),
                bootstrap.getValidatorFactory().getValidator(),
                bootstrap.getMetricRegistry(),
                bootstrap.getClassLoader());
        configuration.getMetricsFactory().configure(environment.lifecycle(),
                bootstrap.getMetricRegistry());
        bootstrap.run(configuration, environment);

        href = namespace.getString("href");

        try {
            cleanupAsynchronously();

        } catch (Exception e) {
            cleanup();
            throw e;
        }

        application.run(configuration, environment);
    }
}