package discord.dao.gitea.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
public class GiteaUser implements GiteaData {
    private String username;
    private int id;
}
