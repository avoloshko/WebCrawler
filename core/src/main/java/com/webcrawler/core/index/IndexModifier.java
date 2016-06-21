package com.webcrawler.core.index;

import java.net.URI;

/**
 * The interface provides write access to index
 */
public interface IndexModifier extends Index {

    void updatePageInfo(URI uri, IndexPageInfo page);

    void init();

    void terminate();
}
