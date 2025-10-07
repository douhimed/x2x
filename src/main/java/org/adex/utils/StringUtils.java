package org.adex.utils;

import java.util.Objects;

public final class StringUtils {

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
