package org.musicplace.playList.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchHighlightUtil {

    public static String highlight(String text, String keyword) {
        if (text == null || keyword == null || keyword.isBlank()) {
            return text;
        }
        String escaped = Pattern.quote(keyword);
        Pattern pattern = Pattern.compile(escaped, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        return matcher.replaceAll(matchResult -> "<em>" + matchResult.group() + "</em>");
    }
}
