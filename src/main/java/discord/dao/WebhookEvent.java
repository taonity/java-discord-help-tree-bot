package discord.dao;

import discord.dao.gitea.api.GiteaUser;
import discord.dao.gitea.api.Repo;
import lombok.Data;

@Data
public class WebhookEvent {
    private Repo repository;
    private GiteaUser pusher;
}
