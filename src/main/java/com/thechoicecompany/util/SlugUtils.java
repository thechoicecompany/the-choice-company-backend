package com.thechoicecompany.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtils {

    private static final Pattern NON_LATIN    = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE   = Pattern.compile("[\\s]+");
    private static final Pattern MULTI_DASH   = Pattern.compile("-{2,}");

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return NON_LATIN.matcher(
                    MULTI_DASH.matcher(
                        WHITESPACE.matcher(normalized.toLowerCase().trim())
                                  .replaceAll("-"))
                              .replaceAll("-"))
                        .replaceAll("")
                        .replaceAll("^-+|-+$", "");
    }
}
