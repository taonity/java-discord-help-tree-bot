package discord.dto.gitea.api;

import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.nio.charset.StandardCharsets;

@Data
@Jacksonized
public class ContentsResponse {
    private byte[] content;

    private String encoding;

    public String getContentAsString() {
        return new String(content, StandardCharsets.UTF_8);
    }
}
