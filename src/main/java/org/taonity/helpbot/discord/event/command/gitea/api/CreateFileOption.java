package org.taonity.helpbot.discord.event.command.gitea.api;

import java.nio.charset.StandardCharsets;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CreateFileOption {
    private final byte[] content;

    public CreateFileOption(String content) {
        this(content.getBytes(StandardCharsets.UTF_8));
    }
}
