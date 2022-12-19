package discord.model;

import discord.structure.ChannelRole;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class GuildSettings {
    @Id
    @Column(name = "guildId")
    private String id;
    private String logChannelId;
    private String helpChannelId;
    private int giteaUserId;

    public String getChannelId(ChannelRole channelRole) {
        switch (channelRole) {
            case HELP:
                return helpChannelId;
            case LOG:
                return logChannelId;
            default:
                return null;
        }
    }
}
