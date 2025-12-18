package io.gdcc.spi.export.dcat3.config.validate;

import java.util.List;

public interface Validator<T> {
    List<ValidationMessage> validate(T input);
}
