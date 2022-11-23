package discord.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class GuildSettings {
    @Id
    @Column(name = "guildId")
    private String id;
    private String logChannelId;
    private String helpChannelId;
}
