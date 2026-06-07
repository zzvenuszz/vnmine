package com.vnmine.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ColorUtils - Chuyển đổi & → § và hỗ trợ tên màu
 * Hỗ trợ: &0-&f, &l&o&n&m&k&r, &red, &blue, &{#hex}...
 */
public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&\\{#([A-Fa-f0-9]{6})}");
    private static final Pattern AMPERSAND_PATTERN = Pattern.compile("&([0-9a-fklmnor])");

    private static final Map<String, String> COLOR_NAMES = new LinkedHashMap<>();

    static {
        // Màu sắc theo tên (giống plugin chat thông dụng)
        COLOR_NAMES.put("&black", "&0");
        COLOR_NAMES.put("&dark_blue", "&1");
        COLOR_NAMES.put("&dark_green", "&2");
        COLOR_NAMES.put("&dark_aqua", "&3");
        COLOR_NAMES.put("&dark_red", "&4");
        COLOR_NAMES.put("&dark_purple", "&5");
        COLOR_NAMES.put("&gold", "&6");
        COLOR_NAMES.put("&gray", "&7");
        COLOR_NAMES.put("&dark_gray", "&8");
        COLOR_NAMES.put("&blue", "&9");
        COLOR_NAMES.put("&green", "&a");
        COLOR_NAMES.put("&aqua", "&b");
        COLOR_NAMES.put("&red", "&c");
        COLOR_NAMES.put("&light_purple", "&d");
        COLOR_NAMES.put("&yellow", "&e");
        COLOR_NAMES.put("&white", "&f");
        COLOR_NAMES.put("&magic", "&k");
        COLOR_NAMES.put("&bold", "&l");
        COLOR_NAMES.put("&strikethrough", "&m");
        COLOR_NAMES.put("&underline", "&n");
        COLOR_NAMES.put("&italic", "&o");
        COLOR_NAMES.put("&reset", "&r");
    }

    /**
     * Chuyển đổi chuỗi có chứa & thành § (màu Minecraft)
     * Hỗ trợ: &0-&f, &l&o&n&m&k, &red, &blue..., &{#hex}
     */
    public static String colorize(String text) {
        if (text == null || text.isEmpty()) return text;

        String result = text;

        // Chuyển tên màu (&red → &c)
        for (Map.Entry<String, String> entry : COLOR_NAMES.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        // Chuyển hex color &{#FF0000}
        Matcher hexMatcher = HEX_PATTERN.matcher(result);
        StringBuffer hexBuffer = new StringBuffer();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(hexBuffer, "§x§" +
                    hexMatcher.group(1).charAt(0) + "§" +
                    hexMatcher.group(1).charAt(1) + "§" +
                    hexMatcher.group(1).charAt(2) + "§" +
                    hexMatcher.group(1).charAt(3) + "§" +
                    hexMatcher.group(1).charAt(4) + "§" +
                    hexMatcher.group(1).charAt(5));
        }
        hexMatcher.appendTail(hexBuffer);
        result = hexBuffer.toString();

        // Chuyển & → §
        result = AMPERSAND_PATTERN.matcher(result).replaceAll("§$1");

        return result;
    }

    /**
     * Chuyển đổi List<String> (từ config/lore)
     */
    public static List<String> colorize(List<String> texts) {
        if (texts == null) return new ArrayList<>();
        return texts.stream()
                .map(ColorUtils::colorize)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi String[] 
     */
    public static String[] colorize(String[] texts) {
        if (texts == null) return new String[0];
        String[] result = new String[texts.length];
        for (int i = 0; i < texts.length; i++) {
            result[i] = colorize(texts[i]);
        }
        return result;
    }

    /**
     * Tạo Component từ chuỗi có &color
     */
    public static Component toComponent(String text) {
        if (text == null) return Component.empty();
        return LegacyComponentSerializer.legacySection()
                .deserialize(colorize(text));
    }

    /**
     * Tạo Component từ List<String>
     */
    public static List<Component> toComponent(List<String> texts) {
        if (texts == null) return new ArrayList<>();
        return texts.stream()
                .map(ColorUtils::toComponent)
                .collect(Collectors.toList());
    }

    /**
     * Tạo Component từ String[] 
     */
    public static Component[] toComponent(String[] texts) {
        if (texts == null) return new Component[0];
        List<Component> list = new ArrayList<>();
        for (String text : texts) {
            list.add(toComponent(text));
        }
        return list.toArray(new Component[0]);
    }

    /**
     * Strip màu khỏi chuỗi
     */
    public static String stripColor(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(text);
    }

    /**
     * Thêm màu gradient đơn giản từ màu start đến end
     */
    public static String gradient(String text, String startColor, String endColor) {
        // Chuyển & color code sang màu hex để tính gradient
        // Implementation đơn giản: chỉ tô màu dần
        StringBuilder sb = new StringBuilder();
        int length = text.length();
        if (length <= 1) return colorize(startColor + text);

        char[] startChars = colorize(startColor).toCharArray();
        char[] endChars = colorize(endColor).toCharArray();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);
            // Đơn giản: chuyển dần từ start sang end
            String code;
            if (ratio < 0.33) {
                code = startColor;
            } else if (ratio < 0.66) {
                code = "&e"; // Vàng trung gian
            } else {
                code = endColor;
            }
            sb.append(colorize(code)).append(text.charAt(i));
        }
        return sb.toString();
    }
}