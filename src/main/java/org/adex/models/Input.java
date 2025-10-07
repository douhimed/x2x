package org.adex.models;

import java.nio.file.Path;

public record Input(Path path, String extension, String dest) {
}
