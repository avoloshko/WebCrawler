package com.webcrawler.core.utils;

import com.google.common.io.ByteStreams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamUtils {

    public static byte[] readBytes(InputStream inputStream, int length)
            throws IOException {
        byte[] buffer = new byte[length];
        int offset = 0;
        while (true) {
            int remained = length - offset;
            if (remained == 0) {
                break;
            }

            int read = inputStream.read(buffer, offset, remained);
            if (read <= 0) break;

            offset += read;
        }

        return buffer;
    }

    public static byte[] readBytes(InputStream inputStream)
            throws IOException {

        NoCopyByteArrayOutputStream bos = new NoCopyByteArrayOutputStream();
        ByteStreams.copy(inputStream, bos);

        return bos.toByteArray();
    }

    public static String readInputStream(InputStream inputStream)
            throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }

        String result = out.toString();

        reader.close();

        return result;
    }
}
