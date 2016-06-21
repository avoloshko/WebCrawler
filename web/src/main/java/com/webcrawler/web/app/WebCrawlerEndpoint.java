package com.webcrawler.web.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webcrawler.core.PageInfo;
import com.webcrawler.core.WebCrawler;
import com.webcrawler.core.WebCrawlerObservable;
import com.webcrawler.core.configuration.Configuration;
import com.webcrawler.core.utils.URIUtils;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

/**
 * WebSocket endpoint which implements message based interface to control WebCrawler and
 * notify clients about crawled pages.
 */
public class WebCrawlerEndpoint extends Endpoint {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private WebCrawler webCrawler;
    private Session session;

    @Override
    public void onOpen(final Session session, EndpointConfig endpointConfig) {
        Configuration configuration = (Configuration) endpointConfig.getUserProperties().get("config");
        webCrawler = new WebCrawler(configuration);
        webCrawler.observable().addObserver(new WebCrawlerObservable.FailureObserver() {
            @Override
            public void onFailed(URI uri) {
                sendMessage("failed", uri.toString());
            }
        });

        webCrawler.observable().addObserver(new WebCrawlerObservable.ProgressObserver() {
            @Override
            public void onPageProcessed(PageInfo pageContext) {
                sendMessage("pageProcessed", pageContext);
            }
        });

        webCrawler.observable().addObserver(new WebCrawlerObservable.CompleteObserver() {
            @Override
            public void onCompleted(int completedCount, int totalCount) {
                sendMessage("completed", new int[] { completedCount, totalCount });
            }
        });

        this.session = session;

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                WebCrawlerEndpoint.this.onMessage(message);
            }
        });
    }

    public void onClose(Session session, CloseReason closeReason) {
        webCrawler.terminate();
    }

    private static class Event<T> {
        String type;

        T payload;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public T getPayload() {
            return payload;
        }

        public void setPayload(T payload) {
            this.payload = payload;
        }
    }

    private void onMessage(String message) {
        Event event;
        try {
            event = objectMapper.readValue(message, Event.class);
        } catch (IOException e) {
            sendAlert("Wrong event: " + message);
            return;
        }

        switch (event.getType()) {
            case "crawl":
                crawl((String) event.getPayload());
                break;
            case "cancel":
                cancel();
                break;
        }
    }

    private void crawl(String uriString) {
        URI uri = URIUtils.normalizeLink(uriString, null, false);
        if (uri != null) {
            webCrawler.crawl(uri);
            return;
        }

        sendAlert("Wrong URI: " + uriString);
    }

    private void cancel() {
        webCrawler.cancel();
    }

    private void sendMessage(String type, Object payload) {
        Event<Object> messageObject = new Event<>();
        messageObject.setType(type);
        messageObject.setPayload(payload);

        try {
            String message = objectMapper.writeValueAsString(messageObject);
            session.getAsyncRemote().sendText(message);
        } catch (JsonProcessingException ignored) {
        }
    }

    private void sendAlert(String text) {
        sendMessage("alert", text);
    }
}