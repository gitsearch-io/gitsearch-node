package io.gitsearch.messagequeue.dao;

import io.gitsearch.messagequeue.Queue;

import java.io.IOException;
import java.util.function.Consumer;

public interface MessageQueueDAO {
    void setConsumer(Queue queue, Consumer<String> message) throws IOException;
}
