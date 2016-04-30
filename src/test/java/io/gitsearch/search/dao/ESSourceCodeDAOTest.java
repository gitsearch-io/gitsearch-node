package io.gitsearch.search.dao;

import io.gitsearch.search.dto.FileBranchDTO;
import io.gitsearch.search.dto.SourceFileDTO;
import io.gitsearch.search.dao.setup.AbstractElasticsearchTest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@TestPropertySource(locations="classpath:config.test.properties")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {ESConnection.class, ESSourceCodeDAO.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class ESSourceCodeDAOTest extends AbstractElasticsearchTest{
    private final String ES_INDEX = "gitsearch";
    private final String ES_TYPE = "sourcecode";

    private final String documentID = "1";
    private final String branch = "master";
    private final String filePath = "/path/to/file";
    private final String content = "file content";
    private final String url = "http://repository.com";

    @Autowired
    private ESSourceCodeDAO dao;
    private Client esClient;

    @Before
    public void setUp() throws Exception {
        esClient = getClient();
        boolean indexExists = esClient.admin().indices().prepareExists(ES_INDEX).execute().actionGet().isExists();
        if(!indexExists) {
            esClient.admin().indices().prepareCreate(ES_INDEX).execute().actionGet();
        }
        getClient().admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
    }

    @After
    public void tearDown() throws Exception {
        esClient.admin().indices().prepareDelete(ES_INDEX).execute().actionGet();
    }

    @Test
    public void upsert_should_create_document_in_es() throws Exception {
        dao.indexFile(documentID, getSourceFileDTO(new FileBranchDTO(branch, filePath)));

        GetResponse response = esClient.prepareGet(ES_INDEX, ES_TYPE, documentID).get();
        List<Map<String, String>> fileBranches = parseFileBranches(response);
        assertEquals(1, fileBranches.size());
        assertEquals(filePath, fileBranches.get(0).get("filePath"));
        assertEquals(branch, fileBranches.get(0).get("branchName"));
        assertEquals(content, response.getSource().get("content"));
    }

    @Test
    public void upsert_should_update_document_in_es() throws Exception {
        String featureBranch = "feature";

        dao.indexFile(documentID, getSourceFileDTO(new FileBranchDTO(branch, filePath)));
        dao.indexFile(documentID, getSourceFileDTO(new FileBranchDTO(featureBranch, filePath)));

        GetResponse response = esClient.prepareGet(ES_INDEX, ES_TYPE, documentID).get();
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

        dao.removeFileFromIndex(documentID, getSourceFileDTO(new FileBranchDTO(branch, filePath)));

        GetResponse response = esClient.prepareGet(ES_INDEX, ES_TYPE, documentID).get();
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

        dao.removeFileFromIndex(documentID, getSourceFileDTO(new FileBranchDTO(featureBranch, filePath)));

        GetResponse response = esClient.prepareGet(ES_INDEX, ES_TYPE, documentID).get();
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

        esClient.prepareIndex(ES_INDEX, ES_TYPE, documentID).setSource(json).get();
    }

    private List<Map<String, String>> parseFileBranches(GetResponse response) {
        return (List<Map<String, String>>)response.getSource().get("fileBranches");
    }

    private SourceFileDTO getSourceFileDTO(FileBranchDTO fileBranchDTO) {
        return new SourceFileDTO()
                .setUrl(url)
                .setContent(content)
                .addFileBranch(fileBranchDTO);
    }
}
