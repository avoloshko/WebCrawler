package com.webcrawler.core;

import com.webcrawler.core.parse.HTMLParser;
import com.webcrawler.core.parse.ParserResult;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;

public class ParserImplTest {

    private HTMLParser parser;

    @Before
    public void setUp() {
        parser = new HTMLParser();
    }

    @Test
    public void testInternalReferances() throws IOException {

        String html = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("page1.html"), "UTF-8");

        ParserResult result = parser.parse(URI.create("https://webcrawler.app"), html);

        assertEquals(result.getInternalURIs().size(), 1);

        assertEquals(result.getInternalURIs().iterator().next(), URI.create("https://webcrawler.app/page1/subpage1"));
    }

    @Test
    public void testExternalReferances() throws IOException {

        String html = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("page2.html"), "UTF-8");

        ParserResult result = parser.parse(URI.create("https://webcrawler.app"), html);

        assertEquals(result.getExternalURIs().size(), 1);

        assertEquals(result.getExternalURIs().iterator().next(), URI.create("http://webcrowler.org/page1"));
    }

    @Test
    public void testTitle() throws IOException {

        String html = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("page3.html"), "UTF-8");

        ParserResult result = parser.parse(URI.create("https://webcrawler.app"), html);

        assertEquals(result.getTitle(), "About WebCrawler");
    }

    @Test
    public void testImages() throws IOException {

        String html = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("page4.html"), "UTF-8");

        ParserResult result = parser.parse(URI.create("https://webcrawler.app"), html);

        assertEquals(result.getImages().size(), 1);

        assertEquals(result.getImages().iterator().next(), URI.create("https://webcrawler.app/image1.jpg"));
    }
}
