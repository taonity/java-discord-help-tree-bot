package org.taonity.helpbot.discord;

import lombok.*;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class GuildSettings {
    @Id
    private int id;

    private String guildId;
    private String logChannelId;
    private String helpChannelId;
    private int giteaUserId;
    private String giteaUserAlphanumeric;

    public String getChannelId(ChannelRole channelRole) {
        return switch (channelRole) {
            case HELP -> helpChannelId;
            case LOG -> logChannelId;
        };
    }
}
