package discord.utils;

import com.google.common.collect.Iterables;
import discord.config.PropertyConfig;
import discord.exception.FailedToRemoveGitApiWorkingDirException;
import discord.exception.FailedToSquashCommitsException;
import discord.exception.NoCommitsException;
import discord.localisation.LogMessage;
import discord.services.GitApiService;
import discord.services.GiteaApiService;
import discord.services.GiteaUserService;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static java.util.Optional.ofNullable;

@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class, classes = {
        PropertyConfig.class, GitApiService.class})
public class GitTestManager {

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

    private void removeExistingRepoDir(String repoDir) {
        final var repoFolderExists = Paths.get(repoDir).toFile().exists();
        if(repoFolderExists) {
            try {
                FileUtils.deleteDirectory(new File(repoDir));
            } catch (IOException e) {
                throw new FailedToRemoveGitApiWorkingDirException(LogMessage.ALERT_20031);
            }
        }
    }

    public void removeTestGitRepo(String repoName) {
        final var repoDir = String.format(REPO_FOLDER_FORMAT, reposPath, repoName);
        removeExistingRepoDir(repoDir);
    }

    public void createTestGitRepo(String userName, String repoName, List<CommitState> commitStateList) {
        final var repoDir = String.format(REPO_FOLDER_FORMAT, reposPath, repoName);
        final var repoUri = String.format(gitUriFormat, userName, repoName);
        final var branchRef = String.format(REFS_HEADS_PATH_FORMAT, branchName);
        final var repoFile = repoDir + "/" + "dialog-starter.yaml";

        removeExistingRepoDir(repoDir);

        InitCommand cloneCommand = null;
        try {
            cloneCommand = Git.init()
                    .setDirectory(new File(repoDir))
                    .setInitialBranch(branchName);
        } catch (InvalidRefNameException e) {
            e.printStackTrace();
        }

        try(final var git = cloneCommand.call()) {
            git.remoteAdd()
                    .setName(REMOTE_NAME)
                    .setUri(new URIish(repoUri))
                    .call();

            try {
                Files.createFile(new File(repoFile).toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            git.add().addFilepattern(".").call();

            commitStateList.forEach(commitState -> {
                String fileName = "";
                switch (commitState) {
                    case VALID:
                        fileName = "dialog-starter.yaml";
                        break;
                    case INVALID:
                        fileName = "dialog-starter-failed.yaml";
                        break;
                }
                final InputStream inputStream;
                try {
                    inputStream = new ClassPathResource(fileName).getInputStream();
                    File file = new File(repoFile);
                    Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    git.commit()
                            .setMessage(COMMIT_MESSAGE)
                            .setAuthor(gitUsername, gitEmail)
                            .setCommitter(gitUsername, gitEmail)
                            .setAll(true)
                            .call();
                } catch (GitAPIException e) {
                    e.printStackTrace();
                }

            });


            git.push()
                    .setRemote(REMOTE_NAME)
                    .add(branchName)
                    .setForce(true)
                    .setCredentialsProvider(
                            new UsernamePasswordCredentialsProvider(gitUsername, gitPassword)
                    ).call();


        } catch (GitAPIException | URISyntaxException e) {
            e.printStackTrace();
            throw new FailedToRemoveGitApiWorkingDirException(LogMessage.ALERT_20033);
        } finally {
            removeExistingRepoDir(repoDir);
        }
    }


}
