package io.gitsearch.messagequeue;

import io.gitsearch.db.RepositoryService;
import io.gitsearch.git.GitRepositoryService;
import io.gitsearch.git.GitService;
import io.gitsearch.messagequeue.dao.MessageQueueDAO;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {
    @Mock
    private MessageQueueDAO messageQueueDAOMock;
    @Mock
    private GitRepositoryService gitRepositoryServiceMock;
    @Mock
    private GitService gitServiceMock;
    @Mock
    private RepositoryService repositoryServiceMock;
    @Mock
    private Git gitMock;

    @InjectMocks
    private MessageService messageService;

    private final String URL = "https://foo.bar";
    private final List<String> BRANCHES = Arrays.asList("master", "feature");

    @Test
    public void updateRepository_should_get_git_repo_and_update_database_with_changes() throws IOException, GitAPIException {
        when(gitRepositoryServiceMock.getRepository(URL)).thenReturn(gitMock);
        when(gitServiceMock.pullUpdates(gitMock, URL)).thenReturn(true);
        when(gitServiceMock.getBranchNames(gitMock)).thenReturn(BRANCHES);

        messageService.updateRepository(URL);

        verify(gitRepositoryServiceMock).getRepository(URL);
        verify(gitServiceMock).pullUpdates(gitMock, URL);
        verify(gitServiceMock).getBranchNames(gitMock);
        verify(repositoryServiceMock).update(URL, BRANCHES);
        verifyNoMoreInteractions(gitRepositoryServiceMock, gitServiceMock, repositoryServiceMock);
    }

    @Test
    public void updateRepository_should_get_git_repo() throws IOException {
        when(gitRepositoryServiceMock.getRepository(URL)).thenReturn(gitMock);
        when(gitServiceMock.pullUpdates(gitMock, URL)).thenReturn(false);

        messageService.updateRepository(URL);

        verify(gitRepositoryServiceMock).getRepository(URL);
        verify(gitServiceMock).pullUpdates(gitMock, URL);
        verifyNoMoreInteractions(gitRepositoryServiceMock, gitServiceMock, repositoryServiceMock);
    }

    @Test
    public void cloneRepository_should_clone_git_repo_and_call_repo_service() throws GitAPIException {
        when(gitRepositoryServiceMock.cloneRepository(URL)).thenReturn(gitMock);
        when(gitServiceMock.getBranchNames(gitMock)).thenReturn(BRANCHES);

        messageService.cloneRepository(URL);

        verify(gitRepositoryServiceMock).cloneRepository(URL);
        verify(gitServiceMock).saveAllFilesInRepository(gitMock, URL);
        verify(gitServiceMock).getBranchNames(gitMock);
        verify(repositoryServiceMock).insert(URL, BRANCHES);
        verifyNoMoreInteractions(gitRepositoryServiceMock, gitServiceMock, repositoryServiceMock);
    }
}
