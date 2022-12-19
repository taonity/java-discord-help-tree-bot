package discord.services;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import discord.exception.FailedToSquashCommitsException;
import discord.exception.NoCommitsException;
import discord.localisation.LogMessage;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Optional.ofNullable;

@Component
public class GitApiService {
    private final static String REPO_FOLDER_FORMAT = "%s%s-folder";
    private final static String REFS_HEADS_PATH_FORMAT = "refs/heads/%s";
    private final static String REMOTE_NAME = "origin";
    private final static String COMMIT_MESSAGE = "Squash failed commits";

    @Value("${gitea.branch-name}")
    private String branchName;

    @Value("${gitea.protocol}://${gitea.address}:${gitea.port}/%s/%s.git")
    private String gitUriFormat;

    @Value("${gitea.git.username}")
    private String gitUsername;

    @Value("${gitea.git.password}")
    private String gitPassword;

    @Value("${gitea.git.email}")
    private String gitEmail;

    @Value("${gitea.git.repo-path}")
    private String reposPath;

    public void squashCommits(String userName, String repoName, int commitQuantity) {
        final var repoDir = String.format(REPO_FOLDER_FORMAT, reposPath, repoName);
        final var repoUri = String.format(gitUriFormat, userName, repoName);
        final var branchRef = String.format(REFS_HEADS_PATH_FORMAT, branchName);
        final var commitQuantityWithResetRefCommit = commitQuantity + 1;

        removeExistingRepoDir(repoDir);

        final var cloneCommand = Git.cloneRepository()
                .setURI(repoUri)
                .setDirectory(new File(repoDir))
                .setBranchesToClone(List.of(branchRef))
                .setBranch(branchRef);

        try(final var git = cloneCommand.call()) {
            final var repo = git.getRepository();
            final var commits = git.log()
                    .add(repo.resolve(branchRef))
                    .setMaxCount(commitQuantityWithResetRefCommit)
                    .call();
            final var lastCommitName = ofNullable(Iterables.getLast(commits))
                    .map(AnyObjectId::getName)
                    .orElseThrow(() -> new NoCommitsException(LogMessage.ALERT_20028));

            git.commit()
                    .setMessage(COMMIT_MESSAGE)
                    .setAuthor(gitUsername, gitEmail)
                    .setCommitter(gitUsername, gitEmail)
                    .call();

            git.reset()
                    .setRef(lastCommitName)
                    .setMode(ResetCommand.ResetType.SOFT)
                    .call();

            git.push()
                    .setRemote(REMOTE_NAME)
                    .add(branchName)
                    .setForce(true)
                    .setCredentialsProvider(
                            new UsernamePasswordCredentialsProvider(gitUsername, gitPassword)
                    ).call();
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
            throw new FailedToSquashCommitsException(LogMessage.ALERT_20033);
        } finally {
            removeExistingRepoDir(repoDir);
        }
    }

    private void removeExistingRepoDir(String repoDir) {
        final var repoFolderExists = Paths.get(repoDir).toFile().exists();
        if(repoFolderExists) {
            try {
                FileUtils.deleteDirectory(new File(repoDir));
            } catch (IOException e) {
                e.printStackTrace();
                throw new FailedToSquashCommitsException(LogMessage.ALERT_20031);
            }
        }
    }

}
