package org.taonity.helpbot.discord.event.command.gitea.api;

import java.util.List;
import lombok.Data;

@Data
public class SearchResult<T extends GiteaData> {
    private List<T> data;
    private boolean ok;
}
