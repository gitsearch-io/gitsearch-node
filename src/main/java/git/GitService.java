package git;

import elasticsearch.ElasticSearch;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitService {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private ElasticSearch es = new ElasticSearch();

    private void saveAllFiles(Git git, String ref) throws IOException {
        Repository repository = git.getRepository();
        Ref head = repository.findRef(ref);
        RevWalk walk = new RevWalk(repository);

        RevCommit commit = walk.parseCommit(head.getObjectId());
        RevTree tree = commit.getTree();

        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        while (treeWalk.next()) {
            ObjectId objectId = treeWalk.getObjectId(0);
            System.out.println("found: " + treeWalk.getPathString());
            es.upsert(objectId.getName(), ref, treeWalk.getPathString(), getFileContent(git, objectId));

        }
    }

    private void updateChangedFiles(Git git, List<DiffEntry> diffs, String branch) throws IOException {
        for(DiffEntry diffEntry : diffs) {
            ObjectId newId = diffEntry.getNewId().toObjectId();
            ObjectId oldId = diffEntry.getOldId().toObjectId();
            switch (diffEntry.getChangeType()) {
                case DELETE:
                    es.delete(oldId.getName(), branch, diffEntry.getOldPath());
                    break;
                case MODIFY:
                    es.delete(oldId.getName(), branch, diffEntry.getOldPath());
                    es.upsert(newId.getName(), branch, diffEntry.getNewPath(), getFileContent(git, newId));
                    break;
                case ADD:
                    es.upsert(newId.getName(), branch, diffEntry.getNewPath(), getFileContent(git, newId));
                    break;
                case RENAME:
                    break;
                default:
                    break;
            }
        }
    }

    private String getFileContent(Git git, ObjectId objectId) throws IOException {
        ObjectLoader objectLoader = git.getRepository().open(objectId);
        return new String(objectLoader.getBytes());
    }

    public void pull(Git git){
        try {
            Map<String, ObjectId> currentWorkTree = getCurrentWorkTrees(git);

            Collection<TrackingRefUpdate> fetchResults = fetch(git);
            for (TrackingRefUpdate update : fetchResults) {
                System.out.println(update.getResult());
                switch (update.getResult()) {
                    case NEW:
                        merge(git, update.getLocalName());
                        saveAllFiles(git, update.getLocalName());
                        break;
                    default:
                        ObjectId oldHead = currentWorkTree.get(update.getLocalName());
                        merge(git, update.getLocalName());
                        ObjectId newHead = getWorkTree(git, update.getLocalName());
                        List<DiffEntry> diffs = getChangedFiles(git, oldHead, newHead);
                        updateChangedFiles(git, diffs, update.getLocalName());
                }
            }
        } catch (GitAPIException | IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private Map<String, ObjectId> getCurrentWorkTrees(Git git) throws GitAPIException, IOException {
        Map<String, ObjectId> workTree = new HashMap<>();
        for(Ref branch : getBranches(git)) {
            workTree.put(branch.getName(), getWorkTree(git, branch.getName()));
        }

        return workTree;
    }

    private ObjectId getWorkTree(Git git, String ref) throws IOException {
        return git.getRepository().resolve(ref + "^{tree}");
    }

    private void merge(Git git, String ref) throws GitAPIException {
        git.reset().setMode(ResetCommand.ResetType.HARD).setRef(ref).call();
    }

    private Collection<TrackingRefUpdate> fetch(Git git) throws GitAPIException {
        FetchResult fetchResult = git.fetch().call();

        return fetchResult.getTrackingRefUpdates();
    }

    private List<DiffEntry> getChangedFiles(Git git, ObjectId oldHead, ObjectId newHead) throws GitAPIException, IOException {
        ObjectReader reader = git.getRepository().newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, oldHead);
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, newHead);
        return git.diff()
                .setNewTree(newTreeIter)
                .setOldTree(oldTreeIter)
                .call();
    }

    public List<Ref> getBranches(Git git) throws GitAPIException {
        return git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
    }

    public void cloneRepository(String url, String name) throws Exception {
        File localPath = new File(name);

//        localPath.delete();

        try (Git result = Git.cloneRepository()
                .setURI(url)
                .setDirectory(localPath)
                .call()) {
            System.out.println("Having repository: " + result.getRepository().getDirectory());
        }
    }

    public Repository getRepository(String path) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        return builder.findGitDir(new File(path)).build();
    }
}
