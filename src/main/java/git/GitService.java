package git;

import elasticsearch.ElasticSearch;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ResetCommand;
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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitService {
    private ElasticSearch es = new ElasticSearch();

    public void saveAllFiles(Repository repo, String ref) throws Exception {

        Ref head = repo.findRef(ref);
        RevWalk walk = new RevWalk(repo);

        RevCommit commit = walk.parseCommit(head.getObjectId());
        RevTree tree = commit.getTree();


        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        while (treeWalk.next()) {
            ObjectId objectId = treeWalk.getObjectId(0);
            System.out.println("found: " + treeWalk.getPathString());
            ObjectLoader loader = repo.open(objectId);
            System.out.println(objectId.getName());

            es.upsert(objectId.getName(), ref, treeWalk.getPathString(), new String(loader.getBytes()));
        }
    }

    public void saveFiles(Git git, List<DiffEntry> diffs, String branch) throws Exception{
        for(DiffEntry diffEntry : diffs) {
            ObjectId newId = diffEntry.getNewId().toObjectId();
            switch (diffEntry.getChangeType()) {
                case DELETE:
                    break;
                case MODIFY:
                    ObjectId oldId = diffEntry.getOldId().toObjectId();
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

    private String getFileContent(Git git, ObjectId objectId) throws Exception{
        ObjectLoader objectLoader = git.getRepository().open(objectId);
        return new String(objectLoader.getBytes());
    }

//    public void pullUpdates(Git git, Repository repo) throws Exception {
//        ObjectId oldHead = repo.resolve("HEAD^{tree}");
//        System.out.println("oldHead: " + oldHead);
//        FetchResult fetchResult = git.fetch().call();
//
//        for(TrackingRefUpdate updated : fetchResult.getTrackingRefUpdates()) {
//            System.out.println(updated.getLocalName());
//            System.out.println(updated.getResult());
//        }
//
//        git.reset().setMode(ResetCommand.ResetType.HARD).setRef("refs/remotes/origin/master").call();
//        ObjectId newHead = repo.resolve("HEAD^{tree}");
//        System.out.println("newHead: " + newHead);
//    }

    public void pull(Git git) throws Exception{
        Map<String, ObjectId> currentWorkTree = getCurrentWorkTrees(git);

        Collection<TrackingRefUpdate> fetchResults = fetch(git);
        for(TrackingRefUpdate update : fetchResults) {
            System.out.println(update.getResult());
            switch (update.getResult()) {
                case NEW:
                    merge(git, update.getLocalName());
                    saveAllFiles(git.getRepository(), update.getLocalName());
                    break;
                default:
                    ObjectId oldHead = currentWorkTree.get(update.getLocalName());
                    merge(git, update.getLocalName());
                    ObjectId newHead = getWorkTree(git, update.getLocalName());
                    List<DiffEntry> diffs = getChangedFiles(git, oldHead, newHead);
                    saveFiles(git, diffs, update.getLocalName());
            }
        }
    }

    private Map<String, ObjectId> getCurrentWorkTrees(Git git) throws Exception{
        Map<String, ObjectId> workTree = new HashMap<>();
        for(Ref branch : getBranches(git)) {
            workTree.put(branch.getName(), getWorkTree(git, branch.getName()));
        }

        return workTree;
    }

    private ObjectId getWorkTree(Git git, String ref) throws Exception{
        return git.getRepository().resolve(ref + "^{tree}");
    }

    private void merge(Git git, String ref) throws Exception {
        git.reset().setMode(ResetCommand.ResetType.HARD).setRef(ref).call();
    }

    private Collection<TrackingRefUpdate> fetch(Git git) throws Exception{
        FetchResult fetchResult = git.fetch().call();

        return fetchResult.getTrackingRefUpdates();
    }

    private List<DiffEntry> getChangedFiles(Git git, ObjectId oldHead, ObjectId newHead) throws Exception{
        ObjectReader reader = git.getRepository().newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, oldHead);
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, newHead);
        List<DiffEntry> diffs = git.diff()
                .setNewTree(newTreeIter)
                .setOldTree(oldTreeIter)
                .call();

        return diffs;
    }

    public List<Ref> getBranches(Git git) throws Exception{
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
