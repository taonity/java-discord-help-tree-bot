package discord.dto.gitea.api;

import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
public class GiteaUser implements GiteaData {
    private String username;
    private int id;
}
