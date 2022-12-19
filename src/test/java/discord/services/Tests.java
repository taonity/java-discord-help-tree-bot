package discord.services;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import discord4j.common.util.Snowflake;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.hashids.Hashids;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Tests {

    @Test
    public void test() throws GitAPIException, IOException {
        DfsRepositoryDescription repoDesc = new DfsRepositoryDescription();
        InMemoryRepository repo = new InMemoryRepository(repoDesc);
        Git git = new Git(repo);
        final var remote = "http://localhost:3000/user_448934652992946176/repo_448934652992946176.git";
        git.fetch()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("d", "d"))
                .setRemote(remote)
                //.setRefSpecs("refs/heads/main")
                //.setInitialBranch("refs/heads/main")
                .setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"))
                .call();
        final var commits = git.log().all().setMaxCount(3).call();
        final var oldestCommit = Iterables.getLast(commits);
        System.out.println(oldestCommit);
        System.out.println(repo.isBare());
        final var r = git.reset().setRef(oldestCommit.getName()).setMode(ResetCommand.ResetType.HARD).call();
        System.out.println(StreamSupport.stream(git.log().all().call().spliterator(), false).collect(Collectors.toList()));
        git.commit().setMessage("bruh").call();
        //git.commit().s
        //git.rebase().setUpstream(oldestCommit).runInteractively(handler);
        //repo.getObjectDatabase();
        //ObjectId lastCommitId = repo.resolve("refs/heads/" + BRANCH);

    }

    @Test
    public void test2() throws GitAPIException, IOException {

        //Git git = new Git(repo);
        final var remote = "http://localhost:3000/user_448934652992946176/repo_448934652992946176.git";
        Git git = null;
        File repot = Paths.get("bruh").toFile();

        if(repot.exists()) {
            FileUtils.deleteDirectory(new File("bruh"));
        }

        git = Git.cloneRepository()
                .setURI(remote)
                .setDirectory(new File("bruh"))
                .setBranchesToClone(Arrays.asList("refs/heads/main"))
                .setBranch("refs/heads/main")
                .call();

        final var repo = git.getRepository();
        //DfsRepositoryDescription repoDesc = new DfsRepositoryDescription();
        //InMemoryRepository repo = new InMemoryRepository(repoDesc);
        //Git git = new Git(repo);
        /*git.fetch()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("d", "d"))
                .setRemote(remote)
                //.setRefSpecs("refs/heads/main")
                //.setInitialBranch("refs/heads/main")
                .setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"))
                .call();*/

        final var commits = git.log().add(repo.resolve("refs/heads/main")).setMaxCount(3).call();
        List<String> result = Lists.newArrayList(commits).stream().map(AnyObjectId::getName).collect(Collectors.toList());
        final var resultStr = Iterables.getLast(result);
        System.out.println(result);
        System.out.println(resultStr);
        System.out.println(repo.isBare());
        final var r = git.reset().setRef(resultStr).setMode(ResetCommand.ResetType.SOFT).call();
        System.out.println(StreamSupport.stream(git.log().all().call().spliterator(), false).collect(Collectors.toList()));
        git.commit().setMessage("bruh").call();
        git.push().setRemote("origin").add("main").setForce(true).setCredentialsProvider(new UsernamePasswordCredentialsProvider("d", "d")).call();
        repo.close();
        FileUtils.deleteDirectory(new File("bruh"));
    }

    @Test
    public void test1() throws GitAPIException, IOException {
        DfsRepositoryDescription repoDesc = new DfsRepositoryDescription();
        final var remote = "http://localhost:3000/user_448934652992946176/repo_448934652992946176.git";

        InMemoryRepository repo = new InMemoryRepository(repoDesc);
        Git git = new Git(repo);
        git.fetch()
                .setRemote(remote)
                .setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"))
                .call();
        repo.getObjectDatabase();

        ObjectId lastCommitId = repo.resolve("refs/heads/" + "main");
        //RevWalk revWalk = new RevWalk(repo);
        //RevCommit commit = revWalk.parseCommit(lastCommitId);

        //ObjectId blobId = repo.resolve("refs/heads/" + "main");
        ObjectInserter objectInserter = repo.newObjectInserter();
        TreeFormatter treeFormatter = new TreeFormatter();

        objectInserter = repo.newObjectInserter();
        byte[] bytes = "Hello World!".getBytes(StandardCharsets.UTF_8);
        ObjectId blobId = objectInserter.insert( Constants.OBJ_BLOB, bytes );
        objectInserter.flush();

        treeFormatter.append( "hello-world.txt", FileMode.REGULAR_FILE, blobId );
        ObjectId treeId = objectInserter.insert( treeFormatter );
        objectInserter.flush();

        CommitBuilder commitBuilder = new CommitBuilder();
        commitBuilder.setTreeId( treeId );
        commitBuilder.setMessage( "My first commit!" );
        PersonIdent person = new PersonIdent( "me", "me@example.com" );
        commitBuilder.setAuthor( person );
        commitBuilder.setCommitter( person );
        commitBuilder.setParentId(lastCommitId);
        objectInserter = repo.newObjectInserter();
        ObjectId commitId = objectInserter.insert( commitBuilder );
        objectInserter.flush();


        TreeWalk treeWalk = new TreeWalk( repo );
        treeWalk.addTree( treeId );
        treeWalk.next();
        String filename = treeWalk.getPathString(); // hello-world.txt
        System.out.println(new String(treeWalk.getObjectReader().open(blobId).getBytes(), StandardCharsets.UTF_8));
        System.out.println(filename);

        lastCommitId = repo.resolve("refs/heads/" + "main");
        RevWalk revWalk = new RevWalk(repo);
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        RevTree tree = commit.getTree();
         treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create("dialog-starter.yaml"));
        System.out.println(StreamSupport.stream(git.log().add(repo.resolve("refs/heads/main")).call().spliterator(), false).collect(Collectors.toList()));

        if (!treeWalk.next()) {
            return;
        }
        ObjectId objectId = treeWalk.getObjectId(0);
        ObjectLoader loader = repo.open(objectId);

        //repo.
        loader.copyTo(System.out);
    }

    @Test
    public void hash() {
        Hashids hashids = new Hashids("this is my salt");
        String id = hashids.encode(Snowflake.of("3832775").asLong());
        System.out.println(Snowflake.of("383277523561086979").asLong());
        long[] numbers = hashids.decode(id);
    }
}
