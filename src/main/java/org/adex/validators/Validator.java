package org.adex.validators;

import java.util.List;
import java.util.function.Consumer;

public sealed interface Validator<T> permits DestinationValidator, SourceValidator {

    default void validate(T e) {
        getRules().forEach(rule -> rule.accept(e));
    }

    List<Consumer<T>> getRules();
}
