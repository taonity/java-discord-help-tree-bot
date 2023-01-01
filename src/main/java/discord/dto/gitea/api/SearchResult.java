package discord.dto.gitea.api;

import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Jacksonized
public class SearchResult<T extends GiteaData> {
    private List<T> data;
    private boolean ok;
}
