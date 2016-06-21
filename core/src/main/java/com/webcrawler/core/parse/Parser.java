package com.webcrawler.core.parse;

import java.io.IOException;
import java.net.URI;

/**
 * Provides interface to parse resources
 */
public interface Parser {

    ParserResult parse(URI uri, byte[] bytes);

    ParserResult parse(URI uri, String html);

    ParserResult parse(URI uri) throws IOException;
}
