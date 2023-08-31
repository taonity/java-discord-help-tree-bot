package discord.automation.services;

import static java.util.Optional.ofNullable;

import com.google.common.collect.Iterables;
import discord.exception.NoCommitsException;
import discord.exception.client.FailedToSquashCommitsException;
import discord.exception.main.FailedToRemoveGitApiWorkingDirException;
import discord.logging.LogMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GitApiTestService {
    private static final String REPO_FOLDER_FORMAT = "%s%s-folder";
    private static final String REFS_HEADS_PATH_FORMAT = "refs/heads/%s";
    private static final String REMOTE_NAME = "origin";
    private static final String COMMIT_MESSAGE = "Squash failed commits";

    @Value("${gitea.branch-name}")
    private String branchName;

    @Value("${gitea.url}/%s/%s.git")
    private String gitUriFormat;

    @Value("${gitea.admin.username}")
    private String adminUsername;

    @Value("${gitea.admin.password}")
    private String adminPassword;

    @Value("${gitea.admin.email}")
    private String adminEmail;

    @Value("${gitea.git.repo-path}")
    private String reposPath;

    public void resetRepo(String userName, String repoName, String guildId) throws NoCommitsException {
        final var repoDir = String.format(REPO_FOLDER_FORMAT, reposPath, repoName);
        final var repoUri = String.format(gitUriFormat, userName, repoName);
        final var branchRef = String.format(REFS_HEADS_PATH_FORMAT, branchName);

        removeExistingRepoDir(repoDir);

        final var cloneCommand = Git.cloneRepository()
                .setURI(repoUri)
                .setDirectory(new File(repoDir))
                .setBranchesToClone(List.of(branchRef))
                .setBranch(branchRef);

        try (final var git = cloneCommand.call()) {
            final var repo = git.getRepository();
            final var commits = git.log().add(repo.resolve(branchRef)).call();
            final var firstCommitName = ofNullable(Iterables.getLast(commits))
                    .map(AnyObjectId::getName)
                    .orElseThrow(() -> new NoCommitsException(LogMessage.ALERT_20028));

            git.commit()
                    .setMessage(COMMIT_MESSAGE)
                    .setAuthor(adminUsername, adminEmail)
                    .setCommitter(adminUsername, adminEmail)
                    .call();

            git.reset()
                    .setRef(firstCommitName)
                    .setMode(ResetCommand.ResetType.HARD)
                    .call();

            git.push()
                    .setRemote(REMOTE_NAME)
                    .add(branchName)
                    .setForce(true)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(adminUsername, adminPassword))
                    .call();
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
            throw new FailedToSquashCommitsException(LogMessage.ALERT_20033, guildId);
        } finally {
            removeExistingRepoDir(repoDir);
        }
    }

    private void removeExistingRepoDir(String repoDir) {
        final var repoFolderExists = Paths.get(repoDir).toFile().exists();
        if (repoFolderExists) {
            try {
                FileUtils.deleteDirectory(new File(repoDir));
            } catch (IOException e) {
                e.printStackTrace();
                throw new FailedToRemoveGitApiWorkingDirException(LogMessage.ALERT_20031);
            }
        }
    }
}
