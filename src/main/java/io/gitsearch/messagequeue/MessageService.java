package io.gitsearch.messagequeue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Repository
public class MessageService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Channel channel;

    public void setConsumer(Queue queue, java.util.function.Consumer<String> message) throws IOException {
        Map<String, Object> args = new HashMap<>();
        args.put("x-priority", queue.getPriority());
        channel.queueDeclare(queue.name(), false, false, false, null);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                logger.info("Message received from message queue: " + envelope.toString() + ": " + new String(body, "UTF-8"));
                try {
                    message.accept(new String(body, "UTF-8"));
                } catch(Exception e) {
                    logger.error(e.toString(), e);
                }
            }
        };

        channel.basicConsume(queue.name(), true, args, consumer);
    }
}
