package discord.dao.gitea.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
public class CreateRepoOption {
    public final String name;
}
