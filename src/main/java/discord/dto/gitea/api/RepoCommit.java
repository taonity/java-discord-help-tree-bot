package discord.dto.gitea.api;

import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
public class RepoCommit {
    private String sha;
}
