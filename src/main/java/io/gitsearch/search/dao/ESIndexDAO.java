package io.gitsearch.search.dao;

import io.gitsearch.search.dto.UpdateDTO;
import io.gitsearch.search.dto.SourceFileDTO;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class ESIndexDAO implements IndexDAO {
    private final String ES_INDEX = "gitsearch";
    private final String ES_TYPE = "codefile";

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private JestClient client;

    public ESIndexDAO(String host) {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(host)
                .multiThreaded(true)
                .build());
        client = factory.getObject();
    }

    @Override
    public void indexFile(String id, SourceFileDTO sourceFileDTO) {
        String script = "ctx._source.fileBranches += branch";
        UpdateDTO updateDTO = new UpdateDTO()
                .setSourceFileDTO(sourceFileDTO)
                .setScript(script)
                .addParam("branch", sourceFileDTO.getFileBranches().get(0));
        System.out.println(updateDTO);
        executeUpdate(id, updateDTO);
    }

    @Override
    public void removeFileFromIndex(String id, SourceFileDTO sourceFileDTO) {
        String script = "ctx._source.fileBranches -= branch; " +
                "if(ctx._source.fileBranches.size() == 0) {ctx.op = \"delete\"}";
        UpdateDTO updateDTO = new UpdateDTO()
                .setScript(script)
                .addParam("branch", sourceFileDTO.getFileBranches().get(0));

        executeUpdate(id, updateDTO);
    }

    private void executeUpdate(String id, UpdateDTO updateDTO) {
        logger.info(id);
        JestResult result = null;
        try {
            result = client.execute(new Update.Builder(updateDTO).index(ES_INDEX).type(ES_TYPE).id(id).build());
        } catch (IOException e) {
            logger.error(e.toString(), e);
        } finally {
            if (result != null && result.getErrorMessage() != null) {
                logger.error("Result from Elastic Search: " + result.getErrorMessage());
            }
        }
    }
}
