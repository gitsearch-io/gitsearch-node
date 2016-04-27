package io.gitsearch.messagequeue.dao.setup;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.io.FileUtils;
import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractRabbitTest {
    private final static String HOST = "localhost";
    private final static int PORT = 2222;
    private final static String CONFIG_FILE_NAME = "qpid-config.json";
    private final static String PASSWORD_FILE_NAME = "qpid-password.properties";

    private static Broker broker;
    private static String workDir;
    private static Channel channel;

    @BeforeClass
    public static void startBroker() throws Exception {
        workDir = FileUtils.getTempDirectoryPath() + "/qpid_tmp";

        BrokerOptions brokerOptions = new BrokerOptions();
        brokerOptions.setConfigProperty("qpid.pass_file", getResourcePath(PASSWORD_FILE_NAME));
        brokerOptions.setConfigProperty("qpid.work_dir", workDir);
        brokerOptions.setInitialConfigurationLocation(getResourcePath(CONFIG_FILE_NAME));

        broker = new Broker();
        broker.startup(brokerOptions);
        initChannel();
    }

    @AfterClass
    public static void stopBroker() throws IOException, TimeoutException {
        channel.close();
        broker.shutdown();
        FileUtils.deleteDirectory(new File(workDir));
    }

    private static String getResourcePath(String fileName) {
        return AbstractRabbitTest.class.getResource(fileName).getPath();
    }

    private static void initChannel() throws IOException, TimeoutException{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public Channel getChannel() throws IOException, TimeoutException {
        return channel;
    }
}
