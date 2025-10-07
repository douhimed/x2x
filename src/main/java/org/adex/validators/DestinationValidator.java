package org.adex.validators;

import org.adex.utils.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class DestinationValidator implements Validator<String> {

    public static final Set<String> VALID_EXT = Set.of("yml", "yaml");

    private static final List<Consumer<String>> RULES = List.of(
            dest -> {
                if (StringUtils.isBlank(dest)) {
                    throw new IllegalStateException("Extension is required and cannot be null.");
                }
            },

            dest -> {
                if (!VALID_EXT.contains(dest)) {
                    throw new IllegalStateException("Invalid destination file extension. Supported extensions are: " + VALID_EXT);
                }
            }
    );

    @Override
    public List<Consumer<String>> getRules() {
        return RULES;
    }
}
