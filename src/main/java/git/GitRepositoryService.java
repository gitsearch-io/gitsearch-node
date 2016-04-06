package git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

public class GitRepositoryService {
    public Git cloneRepository(String url, String name) throws GitAPIException {
        File localPath = new File(name);
//        localPath.delete();
        return Git.cloneRepository()
                .setURI(url)
                .setDirectory(localPath)
                .call();
    }

    public Git getRepository(String path) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.findGitDir(new File(path)).build();

        return new Git(repository);
    }
}
