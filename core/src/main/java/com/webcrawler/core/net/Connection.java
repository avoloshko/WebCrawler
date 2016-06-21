package com.webcrawler.core.net;

import java.io.IOException;

/**
 * The interface represents a connection to request resources
 */
public interface Connection {

    /**
     * Sets previous modification time. Should be called before get().
     * Response.notModified == true in case if the resource has not been modified
     */
    void setModifiedSince(long ifModifiedSince);

    Response get() throws IOException;
}
