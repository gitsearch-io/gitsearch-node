package io.gitsearch.elasticsearch.setup;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.Version;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.script.groovy.GroovyPlugin;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public abstract class  AbstractElasticsearchTest {
    private static final String HTTP_PORT = "9205";
    private static final String HTTP_TRANSPORT_PORT = "9305";
    private static final String ES_WORKING_DIR = "target/es";

    private static Node node;

    @BeforeClass
    public static void startElasticsearch() throws Exception {
        deleteDataDirectory();

        Settings settings = Settings.builder()
                .put("path.home", ES_WORKING_DIR)
                .put("path.conf", ES_WORKING_DIR)
                .put("path.data", ES_WORKING_DIR)
                .put("path.work", ES_WORKING_DIR)
                .put("path.logs", ES_WORKING_DIR)
                .put("http.port", HTTP_PORT)
                .put("transport.tcp.port", HTTP_TRANSPORT_PORT)
                .put("index.number_of_shards", "1")
                .put("index.number_of_replicas", "0")
                .put("discovery.zen.ping.multicast.enabled", "false")
                .put("script.engine.groovy.inline.update", "on")
                .put("name", "name")
                .build();
        Environment env = new Environment(settings);
        Collection<Class<? extends Plugin>> classpathPlugins =  Collections.singletonList(GroovyPlugin.class);
        node = new ConfigurableNode(env, Version.CURRENT, classpathPlugins );

        node.start();
    }

    @AfterClass
    public static void stopElasticsearch() {
        node.close();
        deleteDataDirectory();
    }

    public Client getClient() {
        return node.client();
    }

    private static void deleteDataDirectory() {
        try {
            FileUtils.deleteDirectory(new File(ES_WORKING_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not delete data directory of embedded elasticsearch server", e);
        }
    }
}
