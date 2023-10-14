package org.taonity.helpbot.discord.event.command.gitea.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateHookOptionConfig {
    @JsonProperty("content_type")
    private final String contentType;

    private final String url;
}
