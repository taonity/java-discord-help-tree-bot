package discord.dto.gitea.api;

import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Data
public class CreateUserOption {
    private final String username;
    private final String password;
    private final String email;

    public MultiValueMap<String, String> asMultiValueMap() {
        final var map = new LinkedMultiValueMap<String, String>();
        map.add("user_name", username);
        map.add("email", email);
        map.add("password", password);
        map.add("retype", password);
        return map;
    }
}
