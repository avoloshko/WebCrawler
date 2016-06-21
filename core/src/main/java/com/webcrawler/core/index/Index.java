package com.webcrawler.core.index;

import java.net.URI;
import java.util.Collection;

/**
 * The interface provides access to index
 */
public interface Index {

    Collection<URI> getSiteURIs(URI uri);

    IndexPageInfo getPageInfo(URI uri);
}
