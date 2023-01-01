package discord.dto.gitea.api;

import lombok.Data;

import java.util.List;

@Data
public class CreateHookOption {
    private final CreateHookOptionConfig config;
    private final List<String> events;
    private final String type;
    private final boolean active;
}
