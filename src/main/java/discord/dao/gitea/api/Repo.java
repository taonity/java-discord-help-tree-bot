package discord.dao.gitea.api;

import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
public class Repo implements GiteaData {
    private String name;
    private GiteaUser owner;
}
