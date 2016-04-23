package io.gitsearch.search.dao;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ESConnection {
    @Autowired
    private Environment env;

    @Bean
    public JestClient getJestClient() {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(env.getProperty("elasticsearch.host"))
                .multiThreaded(true)
                .build());
        return factory.getObject();
    }
}
