package discord.utils;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.eclipse.jgit.api.Git;

public class GitManager {

    public static void updateRepo(final String fullRepoPath, final String repoUrl) {
        try {
            File repo = Paths.get(fullRepoPath).toFile();
            if(repo.exists()) {
                Git git = Git.open(repo);
                git.fetch().setCheckFetchedObjects(true).call();
                git.pull().call();
            } else {
                Git.cloneRepository()
                        .setURI(repoUrl)
                        .setDirectory(repo)
                        .call();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }
}
