package org.taonity.helpbot.discord.event.command.positive.config;

import java.util.List;
import lombok.Data;
import org.taonity.helpbot.discord.event.command.gitea.api.Repo;

@Data
public class WebhookEvent {
    private Repo repository;
    private List<Commit> commits;
}
