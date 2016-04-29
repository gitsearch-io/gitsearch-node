package io.gitsearch.messagequeue.dao;

import com.rabbitmq.client.Channel;
import io.gitsearch.messagequeue.Queue;
import io.gitsearch.messagequeue.dao.setup.AbstractRabbitTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@TestPropertySource(locations="classpath:config.test.properties")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {RabbitConnection.class, RabbitMessageQueueDAO.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class RabbitMessageQueueDAOTest extends AbstractRabbitTest {
    @Autowired
    private RabbitMessageQueueDAO dao;

    final String updateMessage = "foo";
    final String cloneMessage = "bar";

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void consumers_should_prioritize_the_clone_queue() throws Exception {
        CountDownLatch latch = new CountDownLatch(3);

        sendMessage(Queue.UPDATE, updateMessage);
        sendMessage(Queue.UPDATE, updateMessage);
        sendMessage(Queue.CLONE, cloneMessage);

        dao.setConsumer(Queue.CLONE, receivedMessage -> {
            assertEquals(cloneMessage, receivedMessage);
            assertEquals(3, latch.getCount());
            latch.countDown();
        });

        dao.setConsumer(Queue.UPDATE, receivedMessage -> {
            latch.countDown();
            assertEquals(updateMessage, receivedMessage);
        });

        assertTrue("Test timed out", latch.await(10, TimeUnit.SECONDS));
    }

    private void sendMessage(Queue queue, String message) throws Exception {
        Channel channel = getChannel();
        channel.queueDeclare(queue.name(), false, false, false, null);
        channel.basicPublish("", queue.name(), null, message.getBytes());
    }
}
