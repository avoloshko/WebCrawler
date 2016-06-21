package com.webcrawler.core.utils;

import java.io.ByteArrayOutputStream;

/**
 * ByteArrayOutputStream which do not copy internal buffer
 */
public class NoCopyByteArrayOutputStream extends ByteArrayOutputStream {

    @Override
    public byte[] toByteArray() {
        return buf;
    }
}