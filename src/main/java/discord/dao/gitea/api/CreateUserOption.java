package discord.dao.gitea.api;

import lombok.Data;

@Data
public class CreateUserOption {
    private final String username;
    private final String password;
    private final String email;
}
