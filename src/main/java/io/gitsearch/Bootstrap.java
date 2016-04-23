package io.gitsearch;

import io.gitsearch.git.GitRepositoryDAO;
import io.gitsearch.git.GitService;
import io.gitsearch.messagequeue.MessageService;
import io.gitsearch.messagequeue.Queue;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("Bootstrap")
public class Bootstrap {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GitRepositoryDAO gitRepositoryDAO;
    @Autowired
    private MessageService messageService;
    @Autowired
    private GitService gitService;


    public static void main (String[] args) throws Exception {
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Bootstrap bootstrap = (Bootstrap) context.getBean("Bootstrap");
        bootstrap.listenToMessageQueue();

    }

    private void listenToMessageQueue() {
        try {
            messageService.setConsumer(Queue.CLONE, this::cloneRepository);
            messageService.setConsumer(Queue.UPDATE, this::updateRepository);
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    private void updateRepository(String message) {
        try (Git git = gitRepositoryDAO.getRepository(message)){
            gitService.pullUpdates(git, message);
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    private void cloneRepository(String message) {
        try (Git git = gitRepositoryDAO.cloneRepository(message)) {
            if(git != null) {
                gitService.saveAllFilesInRepository(git, message);
            }
        } catch (GitAPIException e) {
            logger.error(e.toString(), e);
        }
    }
}
