package discord;

public class LocalizedText {
    private String en;
    private String ru;

    public LocalizedText(String en, String ru) {
        this.en = en;
        this.ru = ru;
    }

    public LocalizedText() {
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getRu() {
        return ru;
    }

    public void setRu(String ru) {
        this.ru = ru;
    }

    public String getTranslatedText(Language language) {
        switch (language) {
            case EN:
                return getEn();
            case RU:
                return getRu();
            default:
                throw new IllegalArgumentException("Undefined localized language");
        }
    }
}
