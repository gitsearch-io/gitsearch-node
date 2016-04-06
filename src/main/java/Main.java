import elasticsearch.ElasticSearchService;
import git.GitRepositoryService;
import git.GitService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

public class Main {
    private static final String REMOTE_URL = "https://github.com/kaaresylow/test.git";
//    private static final String REMOTE_URL = "https://github.com/jquery/jquery.git";



    public static void main (String[] args) throws Exception {
        System.out.println("hello world");
        GitRepositoryService gitRepositoryService = new GitRepositoryService();

        Git git = gitRepositoryService.getRepository("repositories/test");

        ElasticSearchService elasticSearchService = new ElasticSearchService();

//
        GitService gitService = new GitService(git, elasticSearchService);
        gitService.pull();
//        gitService.cloneRepository(REMOTE_URL, "repositories/jquery");
//        Repository repository = gitService.getRepository("repositories/test");
//

//        for(Ref branch : gitService.getBranches(git)) {
//            gitService.saveAllFiles(git, branch.getName());
//        }



//        gitService.pull(git);
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



}
