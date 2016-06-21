package com.webcrawler.core.parse;

import java.net.URI;
import java.util.Collection;

/**
 * The class represents data parsed by Parser interface
 */
public class ParserResult {

    private Collection<URI> images;
    private Collection<URI> internalURIs;
    private Collection<URI> externalURIs;

    private String title;

    public Collection<URI> getImages() {
        return images;
    }

    public void setImages(Collection<URI> images) {
        this.images = images;
    }

    public Collection<URI> getInternalURIs() {
        return internalURIs;
    }

    public void setInternalURIs(Collection<URI> internalURIs) {
        this.internalURIs = internalURIs;
    }

    public Collection<URI> getExternalURIs() {
        return externalURIs;
    }

    public void setExternalURIs(Collection<URI> externalURIs) {
        this.externalURIs = externalURIs;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
