package io.gdcc.spi.export.dcat3.config.validate;

import java.util.Collection;
import java.util.Map;

/** Utility for consistent validation checks: null/blank/trim/empty. */
public final class ValidationUtil {
    private ValidationUtil() {}

    /** s == null OR only whitespace. */
    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** s != null AND has non-whitespace characters. */
    public static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /** Trim text or return empty string if null. */
    public static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    /** Collection is null or empty. */
    public static boolean isNullOrEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    /** Map is null or empty. */
    public static boolean isNullOrEmpty(Map<?, ?> m) {
        return m == null || m.isEmpty();
    }
}
