package discord.dto.gitea.api;

import lombok.Data;

@Data
public class GiteaUser implements GiteaData {
    private String username;
    private int id;
}
