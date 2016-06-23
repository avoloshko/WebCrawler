package com.webcrawler.core;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

/**
 * The class represents a scanned page
 */
public class PageInfo {

    private Collection<URI> images = Collections.emptyList();
    private Collection<URI> internalURIs = Collections.emptyList();
    private Collection<URI> externalURIs = Collections.emptyList();

    private String title;
    private URI uri;

    private int pageSize;

    private long pageLoadTime;

    private boolean notModified;

    public Collection<URI> getImages() {
        return images;
    }

    public void setImages(Collection<URI> images) {
        if (images == null) {
            throw new NullPointerException();
        }
        this.images = images;
    }

    public Collection<URI> getInternalURIs() {
        return internalURIs;
    }

    public void setInternalURIs(Collection<URI> internalURIs) {
        if (internalURIs == null) {
            throw new NullPointerException();
        }
        this.internalURIs = internalURIs;
    }

    public Collection<URI> getExternalURIs() {
        return externalURIs;
    }

    public void setExternalURIs(Collection<URI> externalURIs) {
        if (externalURIs == null) {
            throw new NullPointerException();
        }
        this.externalURIs = externalURIs;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getPageLoadTime() {
        return pageLoadTime;
    }

    public void setPageLoadTime(long pageLoadTime) {
        this.pageLoadTime = pageLoadTime;
    }

    public boolean isNotModified() {
        return notModified;
    }

    public void setNotModified(boolean notModified) {
        this.notModified = notModified;
    }
}
