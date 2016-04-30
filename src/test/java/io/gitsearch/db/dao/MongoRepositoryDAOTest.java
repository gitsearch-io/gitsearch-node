package io.gitsearch.db.dao;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import io.gitsearch.db.dao.setup.AbstractMongoTest;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.Assert.assertEquals;

@TestPropertySource(locations="classpath:config.test.properties")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {MongoConnection.class, MongoRepositoryDAO.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class MongoRepositoryDAOTest extends AbstractMongoTest {
    @Autowired
    public MongoRepositoryDAO dao;

    private MongoDatabase db;

    private final String DATABASE = "gitsearch";
    private final String COLLECTION = "repository";
    private final String url = "url";
    private final List<String> branches = Arrays.asList("master", "feature");

    @Before
    public void setUp() {
        db = getMongoClient().getDatabase(DATABASE);
    }

    @After
    public void tearDown() {
        db.drop();
    }

    @Test
    public void insertRepository_should_insert_document_in_mongo() {
        dao.insertRepository(url, branches);

        FindIterable<Document> iterable = db.getCollection(COLLECTION).find(eq("url", url));
        assertEquals(url, iterable.first().get("url"));
        assertEquals(branches, iterable.first().get("branches"));
    }

    @Test
    public void updateRepository_should_update_document_in_mongo() {
        dao.insertRepository(url, branches);
        List<String> updatedBranches = Arrays.asList("feature2", "feature3");

        dao.updateRepository(url, updatedBranches);

        long count = db.getCollection(COLLECTION).count();
        FindIterable<Document> iterable = db.getCollection(COLLECTION).find(eq("url", url));
        assertEquals(url, iterable.first().get("url"));
        assertEquals(updatedBranches, iterable.first().get("branches"));
        assertEquals(1, count);
    }
}
