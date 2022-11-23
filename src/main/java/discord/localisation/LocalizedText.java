package discord.localisation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LocalizedText {
    private final String en;
    private final String ru;

    public String getTranslatedText(Language language) {
        switch (language) {
            case EN:
                return en;
            case RU:
                return ru;
            default:
                throw new IllegalArgumentException("Undefined localized language");
        }
    }

}
