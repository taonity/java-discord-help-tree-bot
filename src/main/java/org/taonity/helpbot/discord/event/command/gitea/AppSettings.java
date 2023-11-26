package org.taonity.helpbot.discord.event.command.gitea;

import lombok.*;
import org.springframework.data.annotation.Id;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class AppSettings {
    @Id
    private int id;

    private String giteaToken;

    public AppSettings(String giteaToken) {
        this.giteaToken = giteaToken;
    }
}
