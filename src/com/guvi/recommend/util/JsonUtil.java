package com.guvi.recommend.util;

import java.util.*;

/**
 * Lightweight, zero-dependency JSON parser and serializer.
 */
public class JsonUtil {

    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + escapeJson((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escapeJson(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Collection<?> col) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : col) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(item));
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj.getClass().isArray()) {
            StringBuilder sb = new StringBuilder("[");
            Object[] arr = (Object[]) obj;
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(arr[i]));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escapeJson(obj.toString()) + "\"";
    }

    public static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    public static Object parse(String json) {
        if (json == null) return null;
        json = json.trim();
        if (json.isEmpty()) return null;
        return new Parser(json).parseValue();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseMap(String json) {
        Object res = parse(json);
        if (res instanceof Map<?, ?>) {
            return (Map<String, Object>) res;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> parseList(String json) {
        Object res = parse(json);
        if (res instanceof List<?>) {
            return (List<Object>) res;
        }
        return new ArrayList<>();
    }

    private static class Parser {
        private final String src;
        private int pos = 0;

        public Parser(String src) {
            this.src = src;
        }

        private void skipWhitespace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }

        public Object parseValue() {
            skipWhitespace();
            if (pos >= src.length()) return null;
            char c = src.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (c == '-' || Character.isDigit(c)) return parseNumber();
            return null;
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // skip '{'
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == '}') {
                pos++;
                return map;
            }
            while (pos < src.length()) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ':') {
                    pos++;
                }
                skipWhitespace();
                Object val = parseValue();
                map.put(key, val);
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ',') {
                    pos++;
                    skipWhitespace();
                } else if (pos < src.length() && src.charAt(pos) == '}') {
                    pos++;
                    break;
                } else {
                    pos++;
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++; // skip '['
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == ']') {
                pos++;
                return list;
            }
            while (pos < src.length()) {
                skipWhitespace();
                Object val = parseValue();
                list.add(val);
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ',') {
                    pos++;
                    skipWhitespace();
                } else if (pos < src.length() && src.charAt(pos) == ']') {
                    pos++;
                    break;
                } else {
                    pos++;
                }
            }
            return list;
        }

        private String parseString() {
            if (pos >= src.length() || src.charAt(pos) != '"') return "";
            pos++; // skip opening "
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = src.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\' && pos < src.length()) {
                    char esc = src.charAt(pos++);
                    switch (esc) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            if (pos + 4 <= src.length()) {
                                String hex = src.substring(pos, pos + 4);
                                pos += 4;
                                try {
                                    sb.append((char) Integer.parseInt(hex, 16));
                                } catch (Exception ignored) {}
                            }
                        }
                        default -> sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (src.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (src.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            return false;
        }

        private Object parseNull() {
            if (src.startsWith("null", pos)) {
                pos += 4;
            }
            return null;
        }

        private Number parseNumber() {
            int start = pos;
            if (pos < src.length() && (src.charAt(pos) == '-' || src.charAt(pos) == '+')) {
                pos++;
            }
            boolean isDouble = false;
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if (Character.isDigit(c)) {
                    pos++;
                } else if (c == '.' || c == 'e' || c == 'E') {
                    isDouble = true;
                    pos++;
                } else {
                    break;
                }
            }
            String numStr = src.substring(start, pos);
            try {
                if (isDouble) {
                    return Double.parseDouble(numStr);
                } else {
                    long l = Long.parseLong(numStr);
                    if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                        return (int) l;
                    }
                    return l;
                }
            } catch (Exception e) {
                return 0;
            }
        }
    }
}
