package discord.dto.gitea.api;

import java.nio.charset.StandardCharsets;
import lombok.Data;

@Data
public class ContentsResponse {
    private byte[] content;

    private String encoding;

    public String getContentAsString() {
        return new String(content, StandardCharsets.UTF_8);
    }
}
