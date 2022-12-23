package discord.model;

import discord.structure.ChannelRole;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@SequenceGenerator(name = "default_generator", sequenceName = "guild_settings_seq", allocationSize = 1)
public class GuildSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default_generator")
    private int id;
    private String guildId;
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
