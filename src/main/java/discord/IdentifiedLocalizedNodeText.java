package discord;

public class IdentifiedLocalizedNodeText {
    private String id;
    private LocalizedText localizedText;

    public IdentifiedLocalizedNodeText(String id, LocalizedText localizedText) {
        this.id = id;
        this.localizedText = localizedText;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalizedText getLocalizedText() {
        return localizedText;
    }

    public void setLocalizedText(LocalizedText localizedText) {
        this.localizedText = localizedText;
    }
}
