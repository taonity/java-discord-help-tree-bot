package discord.automation.services.dto;

import java.nio.charset.StandardCharsets;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UpdateFileOption {
    private final byte[] content;

    private final String sha;

    public UpdateFileOption(String content, String sha) {
        this(content.getBytes(StandardCharsets.UTF_8), sha);
    }
}
