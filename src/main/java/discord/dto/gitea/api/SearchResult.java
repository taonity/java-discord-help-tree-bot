package discord.dto.gitea.api;

import java.util.List;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
public class SearchResult<T extends GiteaData> {
    private List<T> data;
    private boolean ok;
}
