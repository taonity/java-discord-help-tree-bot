package discord.localisation;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.TreeMap;

@RequiredArgsConstructor
public enum LocalizedMessage {
    CLARIFICATION_MESSAGE(new LocalizedText("Choose a clarifying question", "Выбери уточняющий вопрос")),
    GREETING_MESSAGE(new LocalizedText("Let's see what's bothering you", "Посмотрим, что тебя беспокоит")),
    CHOOSE_LANGUAGE_MESSAGE(new LocalizedText("Choose language", "Выбери язык")),
    HELP_ADVICE_GENERATOR_MESSAGE(new LocalizedText("If you have any questions or need help - write `/question`", "Если у тебя возникли вопросы или тебе нужна помощь - напиши `/question`")),
    TIP_MESSAGE(new LocalizedText("Tip", "Подсказка"));

    private final static String DEFAULT_FORMAT = "%s. %s.";
    private final LocalizedText localizedText;

    public String translate(Language language) {
        return localizedText.getTranslatedText(language);
    }

    public String getMerged() {
        return getMerged(DEFAULT_FORMAT);
    }

    public String getMerged(String format) {
        return String.format(
                format,
                localizedText.getTranslatedText(Language.EN),
                localizedText.getTranslatedText(Language.RU)
        );
    }
}
