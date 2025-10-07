package org.adex.algos;

import org.adex.models.Input;
import org.adex.models.Output;
import org.adex.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

final class Properties2Yml implements FileMorpherAlgo {

    public static final Function<Input, Output> ALGO = in -> {
        try (Stream<String> lines = Files.lines(in.path())) {

            final Map<String, Object> root = new TreeMap<>();

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
        Map<String, Object> currentMap = root;

        for (int i = 0; i < keys.length; i++) {
            String keyPart = keys[i];

            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(.+)\\[(\\d+)]").matcher(keyPart);
            boolean isArrayElement = matcher.matches();

            if (isArrayElement) {
                String arrayKey = matcher.group(1);
                int index = Integer.parseInt(matcher.group(2));

                List<Object> list = (List<Object>) currentMap.computeIfAbsent(arrayKey, k -> new ArrayList<>());

                while (list.size() <= index) {
                    list.add(null);
                }

                if (i == keys.length - 1) {
                    String newValue = value.toLowerCase().replace("_", "-");
                    list.set(index, newValue);
                } else {
                    Map<String, Object> nestedMap = (Map<String, Object>) list.get(index);
                    if (nestedMap == null) {
                        nestedMap = new TreeMap<>();
                        list.set(index, nestedMap);
                    }
                    currentMap = nestedMap;
                }
            } else {
                if (i == keys.length - 1) {
                    String newValue = value.toLowerCase().replace("_", "-");
                    currentMap.put(keyPart, newValue);
                } else {
                    currentMap = (Map<String, Object>) currentMap.computeIfAbsent(keyPart, k -> new TreeMap<>());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void buildContent(Map<String, Object> node, StringBuilder sb, int indentLevel) {
        String indent = "  ".repeat(indentLevel);

        for (var entry : node.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            switch (value) {
                case String str -> sb.append(indent).append(key).append(": ").append(str).append("\n");
                case Map<?, ?> map -> {
                    sb.append(indent).append(key).append(":\n");
                    buildContent((Map<String, Object>) map, sb, indentLevel + 1);
                }
                case List<?> list -> {
                    sb.append(indent).append(key).append(":\n");
                    for (Object item : list) {
                        if (item instanceof Map<?, ?> itemMap) {
                            sb.append(indent).append("  -\n");
                            buildContent((Map<String, Object>) itemMap, sb, indentLevel + 2);
                        } else {
                            sb.append(indent).append("  - ").append(item).append("\n");
                        }
                    }
                }
                case null, default -> sb.append(indent).append(key).append(": ").append(value).append("\n");
            }
        }
    }

    private static boolean isValidKeyValue(String line) {
        return StringUtils.isNotBlank(line) && !line.startsWith("#") && line.contains("=");
    }
}
