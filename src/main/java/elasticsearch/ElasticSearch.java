package elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import elasticsearch.dto.FileBranchDTO;
import elasticsearch.dto.UpdateDTO;
import elasticsearch.dto.UpsertDTO;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Update;

public class ElasticSearch {
    private JestClient client;

    public ElasticSearch() {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder("http://localhost:9200")
                .multiThreaded(true)
                .build());
        client = factory.getObject();
    }

    public void upsert(String id, String branch, String path, String content) throws Exception {
        System.out.println(id);
        UpdateDTO updateDTO = getUpsertUpdateDTO(new FileBranchDTO(branch, path), content);
        executeUpdate(updateDTO, id);
    }

    public void delete(String id, String branch, String path) throws Exception{
        System.out.println(id);
        UpdateDTO updateDTO = getDeleteUpdateDTO(new FileBranchDTO(branch, path));
        executeUpdate(updateDTO, id);
    }

    private void executeUpdate(UpdateDTO updateDTO, String id) throws Exception {
        System.out.println(new ObjectMapper().writeValueAsString(updateDTO));
        JestResult result = client.execute(new Update.Builder(updateDTO).index("gitsearch").type("codefile").id(id).build());
        System.out.println(result.getErrorMessage());
    }

    private UpdateDTO getDeleteUpdateDTO(FileBranchDTO fileBranchDTO) {
        String script = "ctx._source.fileBranches -= branch; " +
                "if(ctx._source.fileBranches.size() == 0) {ctx.op = \"delete\"}";
        UpdateDTO updateDTO = new UpdateDTO();
        updateDTO.setScript(script);
        updateDTO.addParam("branch", fileBranchDTO);

        return updateDTO;
    }

    private UpdateDTO getUpsertUpdateDTO(FileBranchDTO fileBranchDTO, String content) {
        String script = "ctx._source.fileBranches += branch";
        UpdateDTO update = new UpdateDTO();
        update.setScript(script);
        update.addParam("branch", fileBranchDTO);

        UpsertDTO upsert = new UpsertDTO();
        upsert.addFileBranch(fileBranchDTO);
        upsert.setContent(content);

        update.setUpsert(upsert);

        return update;
    }
}
