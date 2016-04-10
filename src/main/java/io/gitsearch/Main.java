package io.gitsearch;

import io.gitsearch.elasticsearch.ElasticSearchService;
import io.gitsearch.git.GitRepositoryService;
import io.gitsearch.git.GitService;
import io.gitsearch.messagequeue.MessageService;
import io.gitsearch.messagequeue.Queue;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private GitRepositoryService gitRepositoryService;
    private ElasticSearchService elasticSearchService;
    private PropertyService PropertyService;

    public static void main (String[] args) throws Exception {
        Main main = new Main();
        main.listenToMessageQueue();
    }

    public Main() {
        PropertyService = new PropertyService();
        gitRepositoryService = new GitRepositoryService();
        elasticSearchService = new ElasticSearchService(PropertyService.getProperty("elasticsearch.host"));
    }

    private void listenToMessageQueue() throws Exception{
        MessageService messageService = new MessageService(PropertyService.getProperty("rabbitmq.host"));
        messageService.setConsumer(Queue.CLONE, this::cloneRepository);
        messageService.setConsumer(Queue.UPDATE, this::updateRepository);
    }

    private void updateRepository(String message) {
        try (Git git = gitRepositoryService.getRepository(message)){
            GitService gitService = new GitService(git, elasticSearchService);
            gitService.pullUpdates();
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    private void cloneRepository(String message) {
        try (Git git = gitRepositoryService.cloneRepository(message)) {
            if(git != null) {
                GitService gitService = new GitService(git, elasticSearchService);
                gitService.saveAllFilesInRepository();
            }
        } catch (GitAPIException e) {
            logger.error(e.toString(), e);
        }
    }
}
