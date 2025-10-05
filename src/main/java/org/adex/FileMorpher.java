package org.adex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public interface FileMorpher {

    Map<String, Map<String, FileMorpherAlgo>> ALGOS = Map.of(
            "properties", Map.of("yml", new Properties2Yml(), "yaml", new Properties2Yml())
    );

    static Output execute(Input in) {
        return ALGOS.get(in.extension()).get(in.dest()).getAlgo().apply(in);
    }
}

final class FileMorpherInitializer {

    private static final Validator<Path> SRC_VALIDATOR = new SrcValidator();
    private static final Validator<String> DEST_VALIDATOR = new DestinationValidator();

    private Path src;
    private String dest;
    private FileMorpher morpher;

    private Output output;

    public FileMorpherInitializer from(String src) {
        this.src = Path.of(src);
        return this;
    }

    public FileMorpherInitializer to(String dest) {
        this.dest = dest;
        return this;
    }

    public FileMorpherInitializer start() {
        validate();
        output = FileMorpher.execute(new Input(src, getExtension(), dest));
        return this;
    }

    public Path path() {
        return output.path();
    }

    public String getFileName() {
        return output.path().toAbsolutePath().toString();
    }

    private void validate() {
        SRC_VALIDATOR.validate(src);
        DEST_VALIDATOR.validate(dest);
    }

    private String getExtension() {
        final String fileName = src.getFileName().toString();
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}

record Input(Path path, String extension, String dest) {
}

record Output(Path path) {
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

    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }
}

sealed interface FileMorpherAlgo permits Properties2Yml {
    Function<Input, Output> getAlgo();
}

final class Properties2Yml implements FileMorpherAlgo {

    public static final Function<Input, Output> ALGO = in -> {
        try (Stream<String> lines = Files.lines(in.path())) {

            final Map<String, Object> root = new HashMap<>();

            lines
                    .filter(Properties2Yml::isValidKeyValue)
                    .map(line -> line.split("=", 2))
                    .forEach(data -> insertData(root, data[0].trim(), data[1].trim()));

            final StringBuilder content = new StringBuilder();
            buildContent(root, content, 0);

            return new Output(Files.writeString(resolveDestinationPath(in), content));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    };

    @Override
    public Function<Input, Output> getAlgo() {
        return ALGO;
    }


    private static Path resolveDestinationPath(Input in) {
        var originalPath = in.path();
        var fileName = originalPath.getFileName().toString();
        var baseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;

        var ext = in.dest().startsWith(".") ? in.dest() : "." + in.dest();

        return originalPath.resolveSibling(baseName + ext);
    }

    @SuppressWarnings("unchecked")
    private static void insertData(Map<String, Object> root, String key, String value) {
        String[] keys = key.split("\\.");
        Map<String, Object> current = root;

        for (int i = 0; i < keys.length; i++) {
            String keyPart = keys[i];
            if (i == keys.length - 1) {
                current.put(keyPart, value);
            } else {
                current = (Map<String, Object>) current.computeIfAbsent(keyPart, k -> new HashMap<String, Object>());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void buildContent(Map<String, Object> node, StringBuilder sb, int indentLevel) {
        String indent = "  ".repeat(indentLevel);

        for (var entry : node.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            if (value instanceof String str) {
                sb.append(indent).append(key).append(": ").append(str).append("\n");
            } else if (value instanceof Map<?, ?> map) {
                sb.append(indent).append(key).append(":\n");
                buildContent((Map<String, Object>) map, sb, indentLevel + 1);
            } else {
                sb.append(indent).append(key).append(": ").append(value).append("\n");
            }
        }
    }

    private static boolean isValidKeyValue(String line) {
        return StringUtils.isNotBlank(line) && !line.startsWith("#") && line.contains("=");
    }
}