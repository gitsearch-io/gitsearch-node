package io.gitsearch.search;

import io.gitsearch.search.dao.SourceCodeDAO;
import io.gitsearch.search.dto.FileBranchDTO;
import io.gitsearch.search.dto.SourceFileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static io.gitsearch.Utils.toBase64;

@Service()
public class SearchService {
    @Autowired
    private SourceCodeDAO sourceCodeDAO;

    public void upsert(String id, String branch, String path, String content, String url) {
        SourceFileDTO sourceFileDTO = new SourceFileDTO()
                .setUrl(url)
                .setContent(content)
                .addFileBranch(new FileBranchDTO(branch, path));
        sourceCodeDAO.indexFile(id + toBase64(url), sourceFileDTO);
    }

    public void delete(String id, String branch, String path, String url) {
        SourceFileDTO sourceFileDTO = new SourceFileDTO()
                .addFileBranch(new FileBranchDTO(branch, path));
        sourceCodeDAO.removeFileFromIndex(id + toBase64(url), sourceFileDTO);
    }
}
