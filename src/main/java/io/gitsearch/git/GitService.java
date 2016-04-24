package io.gitsearch.git;

import io.gitsearch.search.SearchService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GitService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SearchService searchService;

    public boolean pullUpdates(Git git, String url) {
        boolean updated = false;
        try {
            Map<String, ObjectId> currentWorkTree = getCurrentWorkTrees(git);
            Collection<TrackingRefUpdate> fetchResults = fetch(git);

            for (TrackingRefUpdate update : fetchResults) {
                updated = true;
                switch (update.getResult()) {
                    case NEW:
                        merge(git, update.getLocalName());
                        saveAllFilesInBranch(git, url, update.getLocalName());
                        break;
                    default:
                        ObjectId oldHead = currentWorkTree.get(update.getLocalName());
                        merge(git, update.getLocalName());
                        ObjectId newHead = getWorkTree(git, update.getLocalName());
                        List<DiffEntry> diffs = getChangedFiles(git, oldHead, newHead);
                        updateChangedFiles(git, url, diffs, update.getLocalName());
                        break;
                }
            }
        } catch (GitAPIException | IOException e) {
            logger.error(e.getMessage(), e);
        }

        return updated;
    }

    public void saveAllFilesInRepository(Git git, String url) {
        try {
            for (Ref ref: getBranches(git)) {
                saveAllFilesInBranch(git, url, ref.getName());
            }
        } catch (GitAPIException | IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void saveAllFilesInBranch(Git git, String url, String ref) throws IOException {
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
            searchService.upsert(objectId.getName(),
                    getBranchName(ref),
                    treeWalk.getPathString(),
                    getFileContent(git, objectId),
                    url);
        }
    }

    private void updateChangedFiles(Git git, String url, List<DiffEntry> diffs, String branch) throws IOException {
        for (DiffEntry diffEntry : diffs) {
            ObjectId newId = diffEntry.getNewId().toObjectId();
            ObjectId oldId = diffEntry.getOldId().toObjectId();
            switch (diffEntry.getChangeType()) {
                case DELETE:
                    searchService.delete(oldId.getName(),
                            branch,
                            diffEntry.getOldPath(),
                            url);
                    break;
                case MODIFY:
                    searchService.delete(oldId.getName(),
                            branch,
                            diffEntry.getOldPath(),
                            url);
                    searchService.upsert(newId.getName(),
                            branch,
                            diffEntry.getNewPath(),
                            getFileContent(git, newId),
                            url);
                    break;
                case ADD:
                    searchService.upsert(newId.getName(),
                            branch,
                            diffEntry.getNewPath(),
                            getFileContent(git, newId),
                            url);
                    break;
                case RENAME:
                    logger.error("RENAME is an unknown case");
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

    private List<Ref> getBranches(Git git) throws GitAPIException {
        return git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
    }

    private String getBranchName(String branchRef) {
        final String branchPrefix = "refs/remotes/origin/";
        return branchRef.substring(branchPrefix.length(), branchRef.length());
    }

    public List<String> getBranchNames(Git git) {
        try {
            return getBranches(git)
                    .stream()
                    .map(branchRef -> getBranchName(branchRef.getName()))
                    .collect(Collectors.toList());
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }
}
