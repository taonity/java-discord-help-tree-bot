package discord.dto;

import discord.dto.gitea.api.GiteaUser;
import discord.dto.gitea.api.Repo;
import lombok.Data;

@Data
public class WebhookEvent {
    private Repo repository;
    private GiteaUser pusher;
}
