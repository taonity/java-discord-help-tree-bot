package discord.dao;

import lombok.Data;

@Data
public class GithubWebhookEvent {
    private GithubWebhookSender sender;
}
