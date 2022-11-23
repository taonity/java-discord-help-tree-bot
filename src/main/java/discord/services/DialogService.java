package discord.services;

import discord.tree.Node;
import discord.utils.YamlLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DialogService {
    @Value("/${tree.repo.local-path}/${tree.repo.name}/${tree.repo.config-name}")
    private String dialogFileFullPath;

    private final GitService gitService;

    public Node readTree() {
        gitService.updateTreeConfigRepo();
        return YamlLoader.loadFromFile(dialogFileFullPath, Node.class);
    }
}
