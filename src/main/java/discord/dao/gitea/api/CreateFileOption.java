package discord.dao.gitea.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

@Data
@RequiredArgsConstructor
public class CreateFileOption {
    private final byte[] content;

    public CreateFileOption(String content) {
        this(content.getBytes(StandardCharsets.UTF_8));
    }
}
