package io.gitsearch.elasticsearch;

import io.gitsearch.elasticsearch.setup.AbstractElasticsearchTest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.gitsearch.Utils.toBase64;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;


public class ElasticSearchServiceTest extends AbstractElasticsearchTest {
    private final String ES_URL = "http://localhost:9205";
    private final String ES_INDEX = "gitsearch";
    private final String ES_TYPE = "codefile";

    private String documentID = "1";
    private String branch = "master";
    private String filePath = "/path/to/file";
    private String content = "file content";
    private String url = "http://repository.com";

    private ElasticSearchService elasticSearchService;
    private Client client;

    @Before
    public void setUp() throws Exception {
        client = getClient();
        elasticSearchService = new ElasticSearchService(ES_URL);

        boolean indexExists = client.admin().indices().prepareExists(ES_INDEX).execute().actionGet().isExists();
        if(!indexExists) {
            client.admin().indices().prepareCreate(ES_INDEX).execute().actionGet();
        }
        getClient().admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
    }

    @After
    public void tearDown() throws Exception {
        client.admin().indices().prepareDelete(ES_INDEX).execute().actionGet();
    }

    @Test
    public void upsert_should_create_document_in_es() throws Exception {
        elasticSearchService.upsert(documentID, branch, filePath, content, url);

        GetResponse response = client.prepareGet(ES_INDEX, ES_TYPE, documentID + toBase64(url)).get();
        List<Map<String, String>> fileBranches = parseFileBranches(response);
        assertEquals(1, fileBranches.size());
        assertEquals(filePath, fileBranches.get(0).get("filePath"));
        assertEquals(branch, fileBranches.get(0).get("branchName"));
        assertEquals(content, response.getSource().get("content"));
    }

    @Test
    public void upsert_should_update_document_in_es() throws Exception {
        String featureBranch = "feature";
        elasticSearchService.upsert(documentID, branch, filePath, content, url);
        elasticSearchService.upsert(documentID, featureBranch, filePath, content, url);

        GetResponse response = client.prepareGet(ES_INDEX, ES_TYPE, documentID + toBase64(url)).get();
        List<Map<String, String>> fileBranches = parseFileBranches(response);
        assertEquals(2, fileBranches.size());
        assertEquals(filePath, fileBranches.get(0).get("filePath"));
        assertEquals(branch, fileBranches.get(0).get("branchName"));
        assertEquals(filePath, fileBranches.get(1).get("filePath"));
        assertEquals(featureBranch, fileBranches.get(1).get("branchName"));
        assertEquals(content, response.getSource().get("content"));
    }

    @Test
    public void delete_should_delete_document_from_es() throws Exception {
        Map<String, String> fileBranch = new HashMap<>();
        fileBranch.put("branchName", branch);
        fileBranch.put("filePath", filePath);
        List<Map<String, String>> fileBranches = new ArrayList<>();
        fileBranches.add(fileBranch);
        insertDocument(documentID, content, fileBranches);

        elasticSearchService.delete(documentID, branch, filePath, url);

        GetResponse response = client.prepareGet(ES_INDEX, ES_TYPE, documentID).get();
        assertFalse(response.isExists());
    }

    @Test
    public void delete_should_update_document_in_es() throws Exception {
        Map<String, String> fileBranch1 = new HashMap<>();
        fileBranch1.put("branchName", branch);
        fileBranch1.put("filePath", filePath);

        String featureBranch = "feature";
        Map<String, String> fileBranch2 = new HashMap<>();
        fileBranch2.put("branchName", featureBranch);
        fileBranch2.put("filePath", filePath);

        List<Map<String, String>> fileBranches = new ArrayList<>();
        fileBranches.add(fileBranch1);
        fileBranches.add(fileBranch2);

        insertDocument(documentID, content, fileBranches);

        elasticSearchService.delete(documentID, featureBranch, filePath, url);

        GetResponse response = client.prepareGet(ES_INDEX, ES_TYPE, documentID + toBase64(url)).get();
        List<Map<String, String>> actualFileBranches = parseFileBranches(response);
        assertEquals(1, actualFileBranches.size());
        assertEquals(filePath, actualFileBranches.get(0).get("filePath"));
        assertEquals(branch, actualFileBranches.get(0).get("branchName"));
        assertEquals(content, response.getSource().get("content"));
    }

    private void insertDocument(String documentID, String content, List<Map<String, String>> fileBranches) {
        Map<String, Object> json = new HashMap<>();
        json.put("content", content);
        json.put("fileBranches", fileBranches);

        client.prepareIndex(ES_INDEX, ES_TYPE, documentID + toBase64(url)).setSource(json).get();
    }

    private List<Map<String, String>> parseFileBranches(GetResponse response) {
        return (List<Map<String, String>>)response.getSource().get("fileBranches");
    }
}
