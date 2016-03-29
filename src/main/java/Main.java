import elasticsearch.ElasticSearch;
import git.GitService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    private static final String REMOTE_URL = "https://github.com/kaaresylow/test.git";



    public static void main (String[] args) throws Exception {
        System.out.println("hello world");
//
        GitService gitService = new GitService();
        Repository repository = gitService.getRepository("test");
        Git git = new Git(repository);

//        gitService.pullUpdates(git, repository);

        gitService.pull(git);
//        for(Ref ref : gitService.getBranches(git)) {
//            System.out.println(ref.getName());
//        }
//        gitService.saveAllFiles(repository);

//        for(Ref ref : gitService.getBranches(git)) {
//            System.out.println(ref.getName());
//        }



//        main.cloneRepo(REMOTE_URL);
//
//        Repository repo = main.getRepo("TestGITRepository");
//
//        Git git = new Git(repo);
//        Ref head = repo.findRef("HEAD");
//        RevWalk walk = new RevWalk(repo);
//
//        RevCommit commit = walk.parseCommit(head.getObjectId());
//        RevTree tree = commit.getTree();
//
//
//        TreeWalk treeWalk = new TreeWalk(repo);
//        treeWalk.addTree(tree);
//        treeWalk.setRecursive(true);
//        while (treeWalk.next()) {
//            ObjectId objectId = treeWalk.getObjectId(0);
//            System.out.println("found: " + treeWalk.getPathString());
//            ObjectLoader loader = repo.open(objectId);
//            System.out.println(new String(loader.getBytes()));
//        }
//
//
//

//        main.pullUpdates(git, repo);
//            main.saveAllFiles(repo);
//
//        List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
//
//        System.out.println(repo.getWorkTree().listFiles()[1].listFiles()[0]);
//
//
////        for(Ref ref : branches) {
////
////            System.out.println(ref.getName());
////        }
    }

//    private void saveAllFiles(Repository repo) throws Exception {
//        Ref head = repo.findRef("HEAD");
//        RevWalk walk = new RevWalk(repo);
//
//        RevCommit commit = walk.parseCommit(head.getObjectId());
//        RevTree tree = commit.getTree();
//
//
//        TreeWalk treeWalk = new TreeWalk(repo);
//        treeWalk.addTree(tree);
//        treeWalk.setRecursive(true);
//        while (treeWalk.next()) {
//            ObjectId objectId = treeWalk.getObjectId(0);
//            System.out.println("found: " + treeWalk.getPathString());
//            ObjectLoader loader = repo.open(objectId);
//            System.out.println(objectId.getName());
//            es.addDocument(objectId.getName(), treeWalk.getPathString(), new String(loader.getBytes()));
//        }
//    }
//
//    private void pullUpdates(Git git, Repository repo) throws Exception {
//        ObjectId oldHead = repo.resolve("HEAD^{tree}");
//        git.fetch().call();
//
//        git.reset().setMode(ResetCommand.ResetType.HARD).setRef("refs/remotes/origin/master").call();
//        ObjectId newHead = repo.resolve("HEAD^{tree}");
//    }
//
//    private List<DiffEntry> getChangedFiles(Git git, Repository repo, ObjectId oldHead, ObjectId newHead) throws Exception{
//        ObjectReader reader = repo.newObjectReader();
//        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
//        oldTreeIter.reset(reader, oldHead);
//        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
//        newTreeIter.reset(reader, newHead);
//        List<DiffEntry> diffs = git.diff()
//                .setNewTree(newTreeIter)
//                .setOldTree(oldTreeIter)
//                .call();
//
//        return diffs;
//    }
//
//    private void cloneRepo(String url) throws Exception {
//        File localPath = new File("test");
//
//        localPath.delete();
//
//        System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
//
//        try (Git result = Git.cloneRepository()
//                .setURI(REMOTE_URL)
//                .setDirectory(localPath)
//                .call()) {
//            System.out.println("Having repository: " + result.getRepository().getDirectory());
//        }
//    }
//
//    public Repository getRepo(String path) throws IOException {
//        FileRepositoryBuilder builder = new FileRepositoryBuilder();
////        Repository repository = builder
////                .readEnvironment()
////                .findGitDir()
////                .build();
//        return builder.findGitDir(new File(path)).build();
////        return repository;
//    }


}
