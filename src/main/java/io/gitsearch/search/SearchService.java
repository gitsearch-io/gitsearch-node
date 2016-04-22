package io.gitsearch.search;

import io.gitsearch.search.dao.ESIndexDAO;
import io.gitsearch.search.dao.IndexDAO;
import io.gitsearch.search.dto.FileBranchDTO;
import io.gitsearch.search.dto.SourceFileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.gitsearch.Utils.toBase64;

public class SearchService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private IndexDAO indexDAO;

    public SearchService(String host) {
        indexDAO = new ESIndexDAO(host);
    }

    public void upsert(String id, String branch, String path, String content, String url) {
        SourceFileDTO sourceFileDTO = new SourceFileDTO()
                .setUrl(url)
                .setContent(content)
                .addFileBranch(new FileBranchDTO(branch, path));
        indexDAO.indexFile(id + toBase64(url), sourceFileDTO);
    }

    public void delete(String id, String branch, String path, String url) {
        SourceFileDTO sourceFileDTO = new SourceFileDTO()
                .addFileBranch(new FileBranchDTO(branch, path));
        indexDAO.removeFileFromIndex(id + toBase64(url), sourceFileDTO);
    }
}
