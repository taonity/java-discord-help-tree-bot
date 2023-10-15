package org.taonity.helpbot.discord.event.command.gitea.api;

import java.util.List;
import lombok.Data;

@Data
public class CreateHookOption {
    private final CreateHookOptionConfig config;
    private final List<String> events;
    private final String type;
    private final boolean active;
}
