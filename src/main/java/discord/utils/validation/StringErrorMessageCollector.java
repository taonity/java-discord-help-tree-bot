package discord.utils.validation;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.lang.String.join;

public class StringErrorMessageCollector {
    private final List<String> errors = new ArrayList<>();

    private static final String LABEL_TOO_BIG_FORMAT = "Label \"%s\" is too big";
    private static final String WRONG_TARGET_ID_FORMAT = "TargetId \"%s\" is wrong";

    public void addLabelTooBig(String label) {
        errors.add(format(LABEL_TOO_BIG_FORMAT, label));
    }

    public void addWrongTarget(String label) {
        errors.add(format(WRONG_TARGET_ID_FORMAT, label));
    }

    public String getErrorsAsString() {
        return join("\n", errors);
    }

    public boolean isEmpty() {
        return errors.isEmpty();
    }
}
