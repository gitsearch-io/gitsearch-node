package io.gitsearch.db.dao;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MongoRepositoryDAO implements RepositoryDAO {
    private final String COLLECTION = "repository";

    @Autowired
    private MongoDatabase db;

    @Override
    public void insertRepository(String url, List<String> branches) {
        db.getCollection(COLLECTION).insertOne(
                new Document("url", url)
                    .append("branches", branches));
    }

    @Override
    public void updateRepository(String url, List<String> branches) {
        db.getCollection(COLLECTION).updateOne(
                new Document("url", url),
                new Document("$set", new Document("branches", branches)));
    }
}
