package discord.localisation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Language {
    EN("English"),
    RU("Русский");

    @Getter
    private final String language;

    public static Language valueOfLanguage(String name) {
        for (Language value : values()) if (value.getLanguage().equalsIgnoreCase(name)) return value;
        throw new IllegalArgumentException();
    }
}
