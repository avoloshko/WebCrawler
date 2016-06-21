package com.webcrawler.web.app;

import be.tomcools.dropwizard.websocket.WebsocketBundle;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.webcrawler.core.configuration.Configuration;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.websocket.server.ServerEndpointConfig;
import java.util.EnumSet;

/**
 * Web application which crawls web pages and prints results.
 * Example arguments:
 * server dist/configuration.yml
 * To print help:
 * -h
 */
public class WebApp extends Application<WebApp.AppConfiguration> {

    public static void main(String[] args) throws Exception {
        new WebApp().run(args);
    }

    static class AppConfiguration extends io.dropwizard.Configuration {

        @JsonProperty("webCrawler")
        private Configuration webCrawler;
    }

    private WebsocketBundle<AppConfiguration> websocketBundle;

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor()
                )
        );

        /* Configure objectMapper to instantiate immutable configuration correctly */
        ObjectMapper objectMapper = bootstrap.getObjectMapper();
        objectMapper.registerModule(new JSR310Module());
        bootstrap.getObjectMapper()
                .setVisibilityChecker(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        /* Host static resources */
        bootstrap.addBundle((new AssetsBundle("/assets", "/", "index.html", "static")));

        /* Enable WebSockets support */
        bootstrap.addBundle(websocketBundle = new WebsocketBundle<>());
    }

    @Override
    public void run(AppConfiguration configuration,
                    Environment environment) throws Exception {

        /* Configure WebSockets */
        ServerEndpointConfig serverEndpointConfig = ServerEndpointConfig.Builder.create(WebCrawlerEndpoint.class, "/webCrawler").build();
        serverEndpointConfig.getUserProperties().put("config", configuration.webCrawler);
        websocketBundle.addEndpoint(serverEndpointConfig);

        final FilterRegistration.Dynamic cors =
                environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        /* Configure CORS parameters */
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "*");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        environment.healthChecks().register("dummy", new DummyHealthCheck());
    }
}