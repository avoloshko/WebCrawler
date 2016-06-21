package com.webcrawler.core.utils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Helper class to work with URIs
 */
public class URIUtils {

    /**
     * Normalizes a link: removes all redundant spaces and parameters.
     * Uses parent URI to resolve relative links.
     * @param link absolute or relative link
     * @param parent parent URI. Can be null
     * @param removeParams if true then parameters and anchor tags will be removed from URI
     * @return Returns normalized URI
     */
    public static URI normalizeLink(String link, URI parent, boolean removeParams) {

        link = link.toLowerCase().trim().replace('\\', '/').replace(" ", "%20");

        URI mergedURL;
        if (parent == null) {
            try {
                mergedURL = URI.create(link);
            } catch (Exception ex) {
                return null;
            }
            if (mergedURL.getScheme() == null) {
                /* Absolute URIs should have a scheme */
                return null;
            }
        } else {
            try {
                mergedURL = URI.create(parent.toString() + "/").resolve(link);
            } catch (Exception ex) {
                return null;
            }
        }

        if (removeParams) {
            try {
                mergedURL = new URI(mergedURL.getScheme(), mergedURL.getUserInfo(), mergedURL.getHost(), mergedURL.getPort(), mergedURL.getPath(), null, null);
            } catch (URISyntaxException e) {
                return null;
            }
        }

        StringBuilder stringURL = new StringBuilder(mergedURL.toString());

        while (stringURL.lastIndexOf("/") == stringURL.length() - 1
                || stringURL.lastIndexOf(".") == stringURL.length() - 1) {
            stringURL.replace(stringURL.length() - 1, stringURL.length(), "");
        }

        return URI.create(stringURL.toString());
    }

    public static URI extractHost(URI url) {
        URI host;
        try {
            host = new URI(url.getScheme(), null, url.getHost(), url.getPort(), null, null, null);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }

        return host;
    }
}
