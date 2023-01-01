package discord.dto.gitea.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EditUserOption {
    @JsonProperty("login_name")
    private final String loginName;
    private final String password;

    @JsonProperty("source_id")
    private final int source_id = 0;
}
