package com.webcrawler.core.net;

import java.net.URI;

/**
 * The interface represents a connection to request resources
 */
public interface ConnectionFactory {

    Connection createConnection(URI uri);
}