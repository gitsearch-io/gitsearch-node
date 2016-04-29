package io.gitsearch.search;


import io.gitsearch.search.dao.SourceCodeDAO;
import io.gitsearch.search.dto.FileBranchDTO;
import io.gitsearch.search.dto.SourceFileDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class SearchServiceTest {
    @Mock
    private SourceCodeDAO sourceCodeDAOMock;

    @InjectMocks
    private SearchService searchService;

    private final String fileID = "id";
    private final String indexID = "iddXJs";
    private final String branch = "master";
    private final String path = "path";
    private final String content = "content";
    private final String url = "url";

    @Test
    public void upsert_should_call_indexFile_on_DAO() {
        searchService.upsert(fileID, branch, path, content, url);

        verify(sourceCodeDAOMock).indexFile(indexID, getSourceFileDTO(branch, path, content, url));
        verifyNoMoreInteractions(sourceCodeDAOMock);
    }

    @Test
    public void delete_should_call_removeFileFromIndex_on_DAO() {
        searchService.delete(fileID, branch, path, url);

        verify(sourceCodeDAOMock).removeFileFromIndex(indexID, getSourceFileDTO(branch, path, null, null));
    }

    private SourceFileDTO getSourceFileDTO(String branch, String path, String content, String url) {
        FileBranchDTO fileBranchDTO = new FileBranchDTO(branch, path);
        return new SourceFileDTO()
                .addFileBranch(fileBranchDTO)
                .setContent(content)
                .setUrl(url);
    }

}