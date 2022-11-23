package discord.services;

import discord.utils.GitManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
    public class GitService {
    @Value("https://github.com/${tree.repo.user}/${tree.repo.name}.git")
    private String repoUrl;

    @Value("/${tree.repo.local-path}/${tree.repo.name}")
    private String fullRepoPath;

    public void updateTreeConfigRepo() {
        GitManager.updateRepo(fullRepoPath, repoUrl);
    }

}
