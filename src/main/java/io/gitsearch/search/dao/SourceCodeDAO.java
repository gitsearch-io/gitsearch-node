package io.gitsearch.search.dao;

import io.gitsearch.search.dto.FileBranchDTO;
import io.gitsearch.search.dto.SourceFileDTO;

public interface SourceCodeDAO {
    void indexFile(String id, SourceFileDTO sourceFileDTO);

    void removeFileFromIndex(String id, SourceFileDTO sourceFileDTO);
}
