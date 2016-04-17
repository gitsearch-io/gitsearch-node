package io.gitsearch.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static io.gitsearch.Utils.toBase64;

public class GitRepositoryService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Git cloneRepository(String url) throws GitAPIException {
        File localPath = new File("repositories/" + toBase64(url));
        if(localPath.exists()) {
            logger.error(url + " already exist");
            return null;
        }

        return Git.cloneRepository()
                .setURI(url)
                .setDirectory(localPath)
                .call();
    }

    public Git getRepository(String url) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.findGitDir(new File("repositories/" + toBase64(url))).build();

        return new Git(repository);
    }
}
