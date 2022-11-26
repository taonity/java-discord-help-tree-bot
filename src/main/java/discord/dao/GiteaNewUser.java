package discord.dao;

import lombok.Data;

@Data
public class GiteaNewUser {
    private final String login;
    private final String password;
    private final String email;
}
