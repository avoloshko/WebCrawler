package com.webcrawler.core.configuration;

import com.google.inject.AbstractModule;
import com.webcrawler.core.index.IndexImpl;
import com.webcrawler.core.index.Index;
import com.webcrawler.core.index.IndexModifier;
import com.webcrawler.core.loading.MultiThreadPageLoader;
import com.webcrawler.core.loading.ObservablePageLoader;
import com.webcrawler.core.loading.PageLoader;
import com.webcrawler.core.net.ConnectionFactory;
import com.webcrawler.core.net.HttpConnectionFactory;
import com.webcrawler.core.parse.HTMLParser;
import com.webcrawler.core.parse.Parser;
import com.webcrawler.core.store.KVStore;
import com.webcrawler.core.store.LevelDBStore;
import com.webcrawler.core.store.MemoryStore;

/**
 * The module configures dependencies
 */
public class InjectionModule extends AbstractModule {

    private Configuration configuration;

    public InjectionModule(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {

        bind(ConfigProvider.class).toInstance(new ConfigProvider() {
            @Override
            public Configuration getConfig() {
                return configuration;
            }
        });

        if (configuration.getStore().useMemory()) {
            bind(KVStore.class).to(MemoryStore.class);
        } else {
            Configuration.LevelDB levelDBConfig = configuration.getStore().getLevelDB();
            bind(KVStore.class).toInstance(new LevelDBStore(levelDBConfig.getDatabase(), levelDBConfig.getCacheSize()));
        }

        Class<? extends ObservablePageLoader> pageLoader = MultiThreadPageLoader.class;
        bind(PageLoader.class).to(pageLoader);
        bind(ObservablePageLoader.class).to(pageLoader);
        bind(Index.class).to(IndexImpl.class);
        bind(IndexModifier.class).to(IndexImpl.class);
        bind(Parser.class).to(HTMLParser.class);
        bind(ConnectionFactory.class).toInstance(new HttpConnectionFactory(configuration.getLoader().getResourceTimeout()));
    }
}