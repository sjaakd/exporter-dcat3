package io.gdcc.spi.export.dcat3.config.validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ValidationReport {
    private final List<ValidationMessage> messages = new ArrayList<>();

    public void add(ValidationMessage m) {
        if (m != null) {
            messages.add(m);
        }
    }

    public void addAll(List<ValidationMessage> ms) {
        if (ms != null) {
            messages.addAll(ms);
        }
    }

    public List<ValidationMessage> messages() {
        return Collections.unmodifiableList(messages);
    }

    public boolean hasErrors() {
        return messages.stream().anyMatch(m -> m.severity() == Severity.ERROR);
    }
}
