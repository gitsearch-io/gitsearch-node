package io.gitsearch.db.dao;

import com.mongodb.client.MongoDatabase;
import io.gitsearch.db.dao.setup.AbstractMongoTest;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MongoRepositoryDAOTest extends AbstractMongoTest {
    private MongoDatabase db;

    @Before
    public void setUp() {
        db = getMongoClient().getDatabase("gitsearch");
    }

    @After
    public void tearDown() {
        db.drop();
    }
}