package io.gitsearch.messagequeue;

import io.gitsearch.db.RepositoryService;
import io.gitsearch.git.GitRepositoryService;
import io.gitsearch.git.GitService;
import io.gitsearch.messagequeue.dao.MessageQueueDAO;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MessageService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MessageQueueDAO messageQueueDAO;
    @Autowired
    private GitRepositoryService gitRepositoryService;
    @Autowired
    private GitService gitService;
    @Autowired
    private RepositoryService repositoryService;

    public void listenToMessageQueue() {
        try {
            messageQueueDAO.setConsumer(Queue.CLONE, this::cloneRepository);
            messageQueueDAO.setConsumer(Queue.UPDATE, this::updateRepository);
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    public void updateRepository(String message) {
        try (Git git = gitRepositoryService.getRepository(message)){
            if (gitService.pullUpdates(git, message)) {
                repositoryService.update(message, gitService.getBranchNames(git));
            }
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    public void cloneRepository(String message) {
        try (Git git = gitRepositoryService.cloneRepository(message)) {
            if (git != null) {
                gitService.saveAllFilesInRepository(git, message);
                repositoryService.insert(message, gitService.getBranchNames(git));
            }
        } catch (GitAPIException e) {
            logger.error(e.toString(), e);
        }
    }
}
