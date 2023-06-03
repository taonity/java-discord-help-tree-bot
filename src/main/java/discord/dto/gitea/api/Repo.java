package discord.dto.gitea.api;

import lombok.Data;

@Data
public class Repo implements GiteaData {
    private String name;
    private GiteaUser owner;
}
