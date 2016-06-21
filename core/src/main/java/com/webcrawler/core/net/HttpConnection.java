package com.webcrawler.core.net;

import com.webcrawler.core.utils.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * A Connection with support for HTTP-specific features
 */
public class HttpConnection implements Connection {

    static {
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    long ifModifiedSince;
    URI uri;
    int timeout;

    @Override
    public void setModifiedSince(long ifModifiedSince) {
        this.ifModifiedSince = ifModifiedSince;
    }

    public HttpConnection(URI uri, int timeout) {
        this.uri = uri;
        this.timeout = timeout;
    }

    private HttpURLConnection createConnection(URI uri) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) new URL(uri.toString()).openConnection();

        conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.addRequestProperty("User-Agent", "WebCrowler");
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setDoOutput(false);
        conn.setDoInput(true);

        if (ifModifiedSince != 0) {
            conn.setIfModifiedSince(ifModifiedSince);
        }

        return conn;
    }

    @Override
    public Response get() throws IOException {

        HttpURLConnection conn = createConnection(uri);

        try {
            int code = conn.getResponseCode();

            boolean redirect = false;

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }

            if (redirect) {
                String newUrl = conn.getHeaderField("Location");
                return new HttpConnection(URI.create(newUrl), timeout).get();
            }

            boolean success = false, notModified = false;
            byte[] data = null;
            if (code == HttpURLConnection.HTTP_NOT_MODIFIED) {
                notModified = true;
                success = true;
            } else if (code == HttpURLConnection.HTTP_OK) {
                data = getBytes(conn.getInputStream(), conn.getContentLength());
                success = true;
            } else {
                data = getBytes(conn.getErrorStream(), conn.getContentLength());
            }

            return new Response(success, data, conn.getLastModified(), notModified);
        } finally {
            conn.disconnect();
        }
    }

    static byte[] getBytes(InputStream getInputStream, int contentLength) throws IOException {
        if (contentLength > 0) {
            return StreamUtils.readBytes(getInputStream, contentLength);
        } else {
            return StreamUtils.readBytes(getInputStream);
        }
    }
}
