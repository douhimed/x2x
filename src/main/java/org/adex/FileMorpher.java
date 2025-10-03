package org.adex;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public interface FileMorpher {

    Output execute(Input in);

}

class FileMorpherImpl implements FileMorpher {

    @Override
    public Output execute(Input in) {
        return null;
    }
}


final class FileMorpherInitializer {

    private static final Validator<Path> SRC_VALIDATOR = new SrcValidator();
    private static final Validator<String> DEST_VALIDATOR = new DestinationValidator();

    private Path src;
    private String dest;
    private FileMorpher morpher;

    public FileMorpherInitializer from(String src) {
        this.src = Path.of(src);
        return this;
    }

    public FileMorpherInitializer to(String dest) {
        this.dest = dest;
        return this;
    }

    public FileMorpherInitializer withDefaultMorpher() {
        this.morpher = new FileMorpherImpl();
        return this;
    }

    public FileMorpherInitializer withMorpher(FileMorpher morpher) {
        this.morpher = morpher;
        return this;
    }

    public String start() {
        validate();
        return morpher.execute(new Input(src, dest)).path();
    }

    private void validate() {
        SRC_VALIDATOR.validate(src);
        DEST_VALIDATOR.validate(dest);
    }
}

record Input(Path src, String dest) {
}

record Output(String path) {
}

sealed interface Validator<T> permits DestinationValidator, SrcValidator {

    default void validate(T e) {
        getRules().forEach(rule -> rule.accept(e));
    }

    List<Consumer<T>> getRules();
}

final class SrcValidator implements Validator<Path> {

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

final class DestinationValidator implements Validator<String> {

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

/**
 * Utils Classes
 */
final class StringUtils {

    private StringUtils() {
        throw new RuntimeException("Bruuh...");
    }

    public static boolean isBlank(String s) {
        return Objects.isNull(s) || s.trim().isBlank();
    }
}
