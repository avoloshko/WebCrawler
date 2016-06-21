package com.webcrawler.core;

import com.webcrawler.core.utils.URIUtils;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class URIUtilsTest {

    @Test
    public void testNormalizeLink() {

        String[][] URIs = {
                new String[]{"http://webcraWler.app/HTML", "http://webcrawler.app/html"},
                new String[]{"http://webcraWler.app/HTML?param1=10#foo", "http://webcrawler.app/html"},
                new String[]{" http://webcraWler.app/HTML/ ", "http://webcrawler.app/html"},
                new String[]{" http://webcraWler.app/information////////////// ", "http://webcrawler.app/information"}
        };

        for (String[] uri : URIs) {
            assertEquals(URI.create(uri[1]), URIUtils.normalizeLink(uri[0], null, true));
        }
    }

    @Test
    public void testNormalizeLinkWithDomain() {

        String[][] URIs = {
                new String[]{"page.html", "https://webcrawler.app/p1/p2/page.html"},
                new String[]{"page/", "https://webcrawler.app/p1/p2/page"},
                new String[]{"page/subpage.html", "https://webcrawler.app/p1/p2/page/subpage.html"},
                new String[]{" /", "https://webcrawler.app"},
                new String[]{"//www.webcrawler.com/", "https://www.webcrawler.com"},
                new String[]{"/page/ ", "https://webcrawler.app/page"},
                new String[]{"../ ", "https://webcrawler.app/p1"},
                new String[]{"../page/ ", "https://webcrawler.app/p1/page"},
                new String[]{"../../../ ", "https://webcrawler.app"},
                new String[]{"./ ", "https://webcrawler.app/p1/p2"},
                new String[]{"./page.html ", "https://webcrawler.app/p1/p2/page.html"},
                new String[]{"./page/subpage ", "https://webcrawler.app/p1/p2/page/subpage"},
                new String[]{"./page\\subpage ", "https://webcrawler.app/p1/p2/page/subpage"},
                new String[]{" # ", "https://webcrawler.app/p1/p2"},
                new String[]{" new page ", "https://webcrawler.app/p1/p2/new%20page"}
        };

        for (String[] uri : URIs) {
            assertEquals(URI.create(uri[1]), URIUtils.normalizeLink(uri[0], URI.create("https://webcrawler.app/p1/p2//"), true));
        }
    }
}
