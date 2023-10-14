package org.taonity.helpbot.discord.event.command.gitea.api;

import lombok.Data;

@Data
public class GiteaUser implements GiteaData {
    private String username;
    private int id;
}
