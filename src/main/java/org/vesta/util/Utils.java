package org.vesta.util;

import java.util.Map;
import java.util.stream.Collectors;

public class Utils {
    public static String printMap(Map<String, Object> data) {
        return data.keySet().stream()
                .map(k -> k + "=" + data.get(k))
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
