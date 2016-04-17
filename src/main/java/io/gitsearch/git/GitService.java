package io.gitsearch.git;

import io.gitsearch.elasticsearch.ElasticSearchService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ElasticSearchService elasticSearchService;
    private Git git;
    private String url;

    public GitService(Git git, ElasticSearchService elasticSearchService, String url) {
        this.elasticSearchService = elasticSearchService;
        this.git = git;
        this.url = url;
    }

    public void pullUpdates() {
        try {
            Map<String, ObjectId> currentWorkTree = getCurrentWorkTrees();
            Collection<TrackingRefUpdate> fetchResults = fetch();

            for (TrackingRefUpdate update : fetchResults) {
                switch (update.getResult()) {
                    case NEW:
                        merge(update.getLocalName());
                        saveAllFilesInBranch(update.getLocalName());
                        break;
                    default:
                        ObjectId oldHead = currentWorkTree.get(update.getLocalName());
                        merge(update.getLocalName());
                        ObjectId newHead = getWorkTree(update.getLocalName());
                        List<DiffEntry> diffs = getChangedFiles(oldHead, newHead);
                        updateChangedFiles(diffs, update.getLocalName());
                        break;
                }
            }
        } catch (GitAPIException | IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void saveAllFilesInRepository() {
        try {
            for(Ref ref: getBranches()) {
                saveAllFilesInBranch(ref.getName());
            }
        } catch (GitAPIException | IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void saveAllFilesInBranch(String ref) throws IOException {
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
            elasticSearchService.upsert(objectId.getName(), getBranchName(ref), treeWalk.getPathString(), getFileContent(objectId), url);
        }
    }

    private void updateChangedFiles(List<DiffEntry> diffs, String branch) throws IOException {
        for(DiffEntry diffEntry : diffs) {
            ObjectId newId = diffEntry.getNewId().toObjectId();
            ObjectId oldId = diffEntry.getOldId().toObjectId();
            switch (diffEntry.getChangeType()) {
                case DELETE:
                    elasticSearchService.delete(oldId.getName(), branch, diffEntry.getOldPath(), url);
                    break;
                case MODIFY:
                    elasticSearchService.delete(oldId.getName(), branch, diffEntry.getOldPath(), url);
                    elasticSearchService.upsert(newId.getName(), branch, diffEntry.getNewPath(), getFileContent(newId), url);
                    break;
                case ADD:
                    elasticSearchService.upsert(newId.getName(), branch, diffEntry.getNewPath(), getFileContent(newId), url);
                    break;
                case RENAME:
                    logger.error("RENAME is an unknown case");
                    break;
                default:
                    break;
            }
        }
    }

    private String getFileContent(ObjectId objectId) throws IOException {
        ObjectLoader objectLoader = git.getRepository().open(objectId);
        return new String(objectLoader.getBytes());
    }

    private Map<String, ObjectId> getCurrentWorkTrees() throws GitAPIException, IOException {
        Map<String, ObjectId> workTree = new HashMap<>();
        for(Ref branch : getBranches()) {
            workTree.put(branch.getName(), getWorkTree(branch.getName()));
        }

        return workTree;
    }

    private ObjectId getWorkTree(String ref) throws IOException {
        return git.getRepository().resolve(ref + "^{tree}");
    }

    private void merge(String ref) throws GitAPIException {
        git.reset().setMode(ResetCommand.ResetType.HARD).setRef(ref).call();
    }

    private Collection<TrackingRefUpdate> fetch() throws GitAPIException {
        FetchResult fetchResult = git.fetch().call();

        return fetchResult.getTrackingRefUpdates();
    }

    private List<DiffEntry> getChangedFiles(ObjectId oldHead, ObjectId newHead) throws GitAPIException, IOException {
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

    private List<Ref> getBranches() throws GitAPIException {
        return git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
    }

    private String getBranchName(String branchRef) {
        final String branchPrefix = "refs/remotes/origin/";
        String branchName = branchRef.substring(branchPrefix.length(), branchRef.length());

        return branchName;
    }
}
