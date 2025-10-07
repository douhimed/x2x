package org.adex.validators;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public final class SourceValidator implements Validator<Path> {

    public static final String REQUIRED_EXTENSION = ".properties";

    private static final List<Consumer<Path>> RULES = List.of(
            src -> {
                if (!Files.exists(src)) {
                    throw new IllegalArgumentException("Source file does not exist: " + src);
                }
            },

            src -> {
                if (!Files.isRegularFile(src)) {
                    throw new IllegalArgumentException("Source path exists but is not a regular file: " + src);
                }
            },

            src -> {
                if (!src.toString().toLowerCase().endsWith(REQUIRED_EXTENSION)) {
                    throw new IllegalArgumentException(
                            "Invalid source file extension. Expected a '" + REQUIRED_EXTENSION + "' file: " + src
                    );
                }
            },
            src -> {
                if (!Files.isReadable(src)) {
                    throw new IllegalArgumentException("Source file exists but is not readable: " + src);
                }
            }
    );

    @Override
    public List<Consumer<Path>> getRules() {
        return RULES;
    }
}
