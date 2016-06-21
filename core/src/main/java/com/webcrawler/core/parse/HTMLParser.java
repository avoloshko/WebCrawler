package com.webcrawler.core.parse;

import com.webcrawler.core.utils.URIUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A Parser which parses HTML pages
 */
@Singleton
public class HTMLParser implements Parser {

    private final static Logger logger = LoggerFactory.getLogger(HTMLParser.class);

    @Override
    public ParserResult parse(URI uri, byte[] bytes) {
        Document document;
        try {
            document = Jsoup.parse(new ByteArrayInputStream(bytes), "UTF-8", uri.toString());
            return parse(uri, document);
        } catch (IOException ignored) {
            throw new RuntimeException(ignored);
        }
    }

    @Override
    public ParserResult parse(URI url, String html) {
        Document document = Jsoup.parse(html);

        return parse(url, document);
    }

    @Override
    public ParserResult parse(URI uri) throws IOException {
        Document document = Jsoup.parse(uri.toURL(), 5000);
        return parse(uri, document);
    }

    ParserResult parse(URI uri, Document document) {

        final Set<URI> linksSet = new LinkedHashSet<>();
        final Set<URI> externalLinksSet = new LinkedHashSet<>();
        final Set<URI> imagesSet = new LinkedHashSet<>();

        String host = URIUtils.extractHost(uri).toString();

        // get page title
        String title = document.title();
        /*logger.info("Title: " + title);
        */

        // get all links
        Elements links = document.select("a[href]");
        for (Element link : links) {
            URI normalizedLink = URIUtils.normalizeLink(link.attr("href"), uri, true);
            if (normalizedLink == null) {
                logger.trace("wrong: " + link.attr("href"));
            }
            if (normalizedLink != null) {
                if (normalizedLink.toString().startsWith(host)) {
                    linksSet.add(normalizedLink);
                } else {
                    externalLinksSet.add(normalizedLink);
                }
            }
        }

        /*
        logger.info("Links: " + linksSet.size());
        for (URI link : linksSet) {
            logger.info(link.toString());
        }*/

        /*logger.info("External links: " + externalLinksSet.size());
        for (URI link : externalLinksSet) {
            logger.info(link.toString());
        }*/

        // get all images
        Elements images = document.select("img[src]");
        for (Element image : images) {
            URI normalizedLink = URIUtils.normalizeLink(image.attr("src"), uri, true);
            if (normalizedLink != null) {
                imagesSet.add(normalizedLink);
            }
        }

        /*logger.info("Images: " + imagesSet.size());
        for (URI link : imagesSet) {
            logger.info(link.toString());
        }*/

        ParserResult result = new ParserResult();
        result.setExternalURIs(Collections.unmodifiableCollection(externalLinksSet));
        result.setImages(Collections.unmodifiableCollection(imagesSet));
        result.setInternalURIs(Collections.unmodifiableCollection(linksSet));
        result.setTitle(title);

        return result;
    }
}
