package discord;

import java.util.Map;
import java.util.TreeMap;

public class LocalizedFields {
    private static final Map<String, LocalizedText> field = new TreeMap<>() {{
        put("clar", new LocalizedText("Choose a clarifying question", "Выбери уточняющий вопрос"));
        put("lets", new LocalizedText("Let's see what's bothering you", "Посмотрим, что тебя беспокоит"));
    }};

    public static String get(String name, Language language) {
        return field.get(name).getTranslatedText(language);
    }
}
