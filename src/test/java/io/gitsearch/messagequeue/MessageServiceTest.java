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

    private final String url = "https://foo.bar";
    private final List<String> branches = Arrays.asList("master", "feature");

    @Test
    public void updateRepository_should_get_git_repo_and_update_database_with_changes() throws IOException, GitAPIException {
        when(gitRepositoryServiceMock.getRepository(url)).thenReturn(gitMock);
        when(gitServiceMock.pullUpdates(gitMock, url)).thenReturn(true);
        when(gitServiceMock.getBranchNames(gitMock)).thenReturn(branches);

        messageService.updateRepository(url);

        verify(gitRepositoryServiceMock).getRepository(url);
        verify(gitServiceMock).pullUpdates(gitMock, url);
        verify(gitServiceMock).getBranchNames(gitMock);
        verify(repositoryServiceMock).update(url, branches);
        verifyNoMoreInteractions(gitRepositoryServiceMock, gitServiceMock, repositoryServiceMock);
    }

    @Test
    public void updateRepository_should_get_git_repo() throws IOException {
        when(gitRepositoryServiceMock.getRepository(url)).thenReturn(gitMock);
        when(gitServiceMock.pullUpdates(gitMock, url)).thenReturn(false);

        messageService.updateRepository(url);

        verify(gitRepositoryServiceMock).getRepository(url);
        verify(gitServiceMock).pullUpdates(gitMock, url);
        verifyNoMoreInteractions(gitRepositoryServiceMock, gitServiceMock, repositoryServiceMock);
    }

    @Test
    public void cloneRepository_should_clone_git_repo_and_call_repo_service() throws GitAPIException {
        when(gitRepositoryServiceMock.cloneRepository(url)).thenReturn(gitMock);
        when(gitServiceMock.getBranchNames(gitMock)).thenReturn(branches);

        messageService.cloneRepository(url);

        verify(gitRepositoryServiceMock).cloneRepository(url);
        verify(gitServiceMock).saveAllFilesInRepository(gitMock, url);
        verify(gitServiceMock).getBranchNames(gitMock);
        verify(repositoryServiceMock).insert(url, branches);
        verifyNoMoreInteractions(gitRepositoryServiceMock, gitServiceMock, repositoryServiceMock);
    }
}
