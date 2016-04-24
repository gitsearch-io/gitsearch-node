package io.gitsearch.db.dao;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MongoConnection {
    @Autowired
    private Environment env;

    @Bean
    public MongoDatabase getMongoClient() {
        return new MongoClient(env.getProperty("mongodb.host"),
                Integer.parseInt(env.getProperty("mongodb.port")))
                .getDatabase(env.getProperty("mongodb.db"));
    }
}
