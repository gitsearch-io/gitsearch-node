package io.gitsearch.db.dao.setup;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

public abstract class AbstractMongoTest {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    private static MongoClient mongoClient;
    private static MongodExecutable mongodExecutable;
    private static MongodProcess mongod;

    @BeforeClass
    public static void startMongo() throws IOException {
        MongodStarter starter = MongodStarter.getDefaultInstance();


        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(PORT, Network.localhostIsIPv6()))
                .build();

        mongodExecutable = starter.prepare(mongodConfig);
        mongod = mongodExecutable.start();
        mongoClient = new MongoClient(HOST, PORT);
    }

    @AfterClass
    public static void stopMongo() {
        mongoClient.close();
        mongod.stop();
        mongodExecutable.stop();
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }
}
