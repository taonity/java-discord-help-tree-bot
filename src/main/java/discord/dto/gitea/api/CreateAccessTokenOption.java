package discord.dto.gitea.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CreateAccessTokenOption {
    private final String name;
}
