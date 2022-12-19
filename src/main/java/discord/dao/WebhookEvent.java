package discord.dao;

import discord.dao.gitea.api.GiteaUser;
import lombok.Data;

@Data
public class WebhookEvent {
    private GiteaUser pusher;
}
