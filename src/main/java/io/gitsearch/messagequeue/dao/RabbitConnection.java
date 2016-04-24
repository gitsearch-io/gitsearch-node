package io.gitsearch.messagequeue.dao;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
public class RabbitConnection {
    @Autowired
    private Environment env;

    @Bean
    public Channel getMessageChannel() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(env.getProperty("rabbitmq.host"));
        Connection connection = factory.newConnection();
        return connection.createChannel();
    }
}
