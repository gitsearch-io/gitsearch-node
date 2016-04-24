package io.gitsearch.search.dao;

import io.gitsearch.search.dto.ESUpdateDTO;
import io.gitsearch.search.dto.SourceFileDTO;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class ESSourceCodeDAO implements SourceCodeDAO {
    private final String ES_INDEX = "gitsearch";
    private final String ES_TYPE = "sourcecode";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JestClient client;

    @Override
    public void indexFile(String id, SourceFileDTO sourceFileDTO) {
        String script = "ctx._source.fileBranches += branch";
        ESUpdateDTO ESUpdateDTO = new ESUpdateDTO()
                .setSourceFileDTO(sourceFileDTO)
                .setScript(script)
                .addParam("branch", sourceFileDTO.getFileBranches().get(0));

        executeUpdate(id, ESUpdateDTO);
    }

    @Override
    public void removeFileFromIndex(String id, SourceFileDTO sourceFileDTO) {
        String script = "ctx._source.fileBranches -= branch; " +
                "if(ctx._source.fileBranches.size() == 0) {ctx.op = \"delete\"}";
        ESUpdateDTO ESUpdateDTO = new ESUpdateDTO()
                .setScript(script)
                .addParam("branch", sourceFileDTO.getFileBranches().get(0));

        executeUpdate(id, ESUpdateDTO);
    }

    private void executeUpdate(String id, ESUpdateDTO ESUpdateDTO) {
        logger.info(id);
        JestResult result = null;
        try {
            result = client.execute(new Update.Builder(ESUpdateDTO).index(ES_INDEX).type(ES_TYPE).id(id).build());
        } catch (IOException e) {
            logger.error(e.toString(), e);
        } finally {
            if (result != null && result.getErrorMessage() != null) {
                logger.error("Result from Elastic Search: " + result.getErrorMessage());
            }
        }
    }
}
