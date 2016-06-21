package com.webcrawler.core.store;

import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * A KVStore which stores key-value data persistently in LevelDB local database.
 * It is thread-safe, but multiple processes can not use the same folder
 */
@Singleton
public class LevelDBStore implements KVStore {

    private static final Logger logger = LoggerFactory.getLogger(LevelDBStore.class);

    private String database;

    private long cacheSize;

    private DB db;

    public LevelDBStore(String database, long cacheSize) {
        this.database = database;
        this.cacheSize = cacheSize;
    }

    @Override
    public void open() {
        Options options = new Options();
        options.createIfMissing(true);
        options.cacheSize(cacheSize);
        options.compressionType(CompressionType.SNAPPY);

        try {
            File file = new File(database);
            logger.info("Opening LevelDB: " + file.getAbsolutePath());
            db = JniDBFactory.factory.open(new File(database), options);
            logger.debug(db.getProperty("leveldb.stats"));
        } catch (IOException e) {
            logger.error("Error opening LevelDB ", e);
        }
    }

    @Override
    public void close() {
        try {
            logger.info("Closing LevelDB");
            db.close();
        } catch (IOException e) {
            logger.error("Error closing LevelDB", e);
        }
    }

    @Override
    public boolean isEmpty() {
        try (DBIterator iterator = db.iterator()) {
            iterator.seekToFirst();
            return !iterator.hasNext();
        } catch (IOException ignored) {
            return true;
        }
    }

    @Override
    public void put(byte[] key, byte[] data) {
        db.put(key, data);
    }

    @Override
    public void remove(byte[] key) {
        db.delete(key);
    }

    @Override
    public byte[] get(byte[] key) {
        return db.get(key);
    }

    @Override
    public void iterate(byte[] keyPrefix, KeyValueProcessor kvProcessor) {

        try (DBIterator dbIterator = db.iterator()) {
            for (dbIterator.seekToFirst(); dbIterator.hasNext(); dbIterator.next()) {
                Map.Entry<byte[], byte[]> entry = dbIterator.peekNext();
                byte[] key = entry.getKey();

                boolean found = true;
                for (int i = 0; i < keyPrefix.length; ++i) {
                    if (i == key.length ||
                            key[i] != keyPrefix[i]) {
                        found = false;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }

                if (kvProcessor.process(key, entry.getValue())) {
                    break;
                }
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void iterate(byte[] keyPrefix, KeyProcessor keyProcessor) {
        try (DBIterator dbIterator = db.iterator()) {
            for (dbIterator.seekToFirst(); dbIterator.hasNext(); dbIterator.next()) {
                Map.Entry<byte[], byte[]> entry = dbIterator.peekNext();
                byte[] key = entry.getKey();

                boolean found = true;
                for (int i = 0; i < keyPrefix.length; ++i) {
                    if (i == key.length ||
                            key[i] != keyPrefix[i]) {
                        found = false;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }

                if (keyProcessor.process(key)) {
                    break;
                }
            }
        } catch (IOException ignored) {
        }
    }
}
