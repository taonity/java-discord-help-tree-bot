package discord.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.*;

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
