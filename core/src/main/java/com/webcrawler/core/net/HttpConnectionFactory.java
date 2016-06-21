package com.webcrawler.core.net;

import java.net.URI;

/**
 * A ConnectionFactory which instantiates HttpConnection instances
 */
public class HttpConnectionFactory implements ConnectionFactory {

    int timeout;

    public HttpConnectionFactory(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public HttpConnection createConnection(URI uri) {

        return new HttpConnection(uri, timeout);
    }
}
