package io.gitsearch.elasticsearch;

import io.gitsearch.elasticsearch.dto.FileBranchDTO;
import io.gitsearch.elasticsearch.dto.UpdateDTO;
import io.gitsearch.elasticsearch.dto.UpsertDTO;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.gitsearch.Utils.toBase64;

public class ElasticSearchService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private JestClient client;

    public ElasticSearchService(String host) {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(host)
                .multiThreaded(true)
                .build());
        client = factory.getObject();
    }

    public void upsert(String id, String branch, String path, String content, String url) {
        UpdateDTO updateDTO = getUpsertUpdateDTO(new FileBranchDTO(branch, path), content, url);
        executeUpdate(updateDTO, id + toBase64(url));
    }

    public void delete(String id, String branch, String path, String url) {
        UpdateDTO updateDTO = getDeleteUpdateDTO(new FileBranchDTO(branch, path));
        executeUpdate(updateDTO, id + toBase64(url));
    }

    private void executeUpdate(UpdateDTO updateDTO, String id) {
        logger.info(id);
        JestResult result = null;
        try {
            result = client.execute(new Update.Builder(updateDTO).index("gitsearch").type("codefile").id(id).build());
        } catch (IOException e) {
            logger.error(e.toString(), e);
        } finally {
            if (result != null && result.getErrorMessage() != null) {
                logger.error("Result from Elastic Search: " + result.getErrorMessage());
            }
        }
    }

    private UpdateDTO getDeleteUpdateDTO(FileBranchDTO fileBranchDTO) {
        String script = "ctx._source.fileBranches -= branch; " +
                "if(ctx._source.fileBranches.size() == 0) {ctx.op = \"delete\"}";
        UpdateDTO updateDTO = new UpdateDTO();
        updateDTO.setScript(script);
        updateDTO.addParam("branch", fileBranchDTO);

        return updateDTO;
    }

    private UpdateDTO getUpsertUpdateDTO(FileBranchDTO fileBranchDTO, String content, String url) {
        String script = "ctx._source.fileBranches += branch";
        UpdateDTO update = new UpdateDTO();
        update.setScript(script);
        update.addParam("branch", fileBranchDTO);

        UpsertDTO upsert = new UpsertDTO();
        upsert.addFileBranch(fileBranchDTO);
        upsert.setContent(content);
        upsert.setUrl(url);
        update.setUpsert(upsert);

        return update;
    }
}
