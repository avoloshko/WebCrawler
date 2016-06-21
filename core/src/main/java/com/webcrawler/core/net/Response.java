package com.webcrawler.core.net;

/**
 * The class represents a resource provided by Connection interface
 */
public class Response {

    public static final Response FAILURE = new Response(false, null, 0, false);

    private boolean success;
    private byte[] data;
    private long dateLastModified;
    private boolean notModified;

    public Response(boolean success, byte[] data, long dateLastModified, boolean notModified) {
        this.success = success;
        this.data = data;
        this.dateLastModified = dateLastModified;
        this.notModified = notModified;
    }

    public boolean isSuccess() {
        return success;
    }

    public byte[] getData() {
        return data;
    }

    public long getDateLastModified() {
        return dateLastModified;
    }

    public boolean isNotModified() {
        return notModified;
    }
}