package com.webcrawler.core.index;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

/**
 * The class represents an indexed page
 */
public class IndexPageInfo {

    private Collection<URI> internalURIs = Collections.emptyList();
    private Collection<URI> externalURIs = Collections.emptyList();
    private Collection<URI> images = Collections.emptyList();

    private String title;

    private int pageSize;
    private long timeLoaded;
    private long timeLastModified;

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

    public Collection<URI> getImages() {
        return images;
    }

    public void setImages(Collection<URI> images) {
        if (images == null) {
            throw new NullPointerException();
        }
        this.images = images;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTimeLoaded() {
        return timeLoaded;
    }

    public void setTimeLoaded(long timeLoaded) {
        this.timeLoaded = timeLoaded;
    }

    public long getTimeLastModified() {
        return timeLastModified;
    }

    public void setTimeLastModified(long timeLastModified) {
        this.timeLastModified = timeLastModified;
    }
}
