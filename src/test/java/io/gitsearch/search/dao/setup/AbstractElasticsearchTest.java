package io.gitsearch.search.dao.setup;

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

    private static String workDir;
    private static Node node;

    @BeforeClass
    public static void startElasticsearch() throws Exception {
        workDir = FileUtils.getTempDirectoryPath() + "/es_tmp";
        deleteDataDirectory();

        Settings settings = Settings.builder()
                .put("path.home", workDir)
                .put("path.conf", workDir)
                .put("path.data", workDir)
                .put("path.work", workDir)
                .put("path.logs", workDir)
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
    public static void stopElasticsearch() throws IOException {
        node.close();
        deleteDataDirectory();
    }

    public Client getClient() {
        return node.client();
    }

    private static void deleteDataDirectory() throws IOException {
        FileUtils.deleteDirectory(new File(workDir));
    }
}
