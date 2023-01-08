package discord.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AppSettings {
    @Id
    private final int id = 0;
    private String giteaToken;

    public AppSettings(String giteaToken) {
        this.giteaToken = giteaToken;
    }
}
