package com.moneymantra.ppwecompat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

final class PpweMessages {
    private PpweMessages() {
    }

    static Component command(String messageKey, Map<String, Component> replacements) {
        return prefixed(render(messageKey, "text", replacements));
    }

    static Component command(String messageKey, String colorKey, Map<String, Component> replacements) {
        return prefixed(render(messageKey, colorKey, replacements));
    }

    static Component worldEdit(String messageKey, Map<String, Component> replacements) {
        return prefixed(render(messageKey, "text", replacements));
    }

    static Component filterWand(String messageKey, String colorKey, Map<String, Component> replacements) {
        return prefixed(render(messageKey, colorKey, replacements));
    }

    static MutableComponent text(String text) {
        return literal(text, "text");
    }

    static MutableComponent value(String text) {
        return literal(text, "value");
    }

    static MutableComponent muted(String text) {
        return literal(text, "muted");
    }

    static MutableComponent success(String text) {
        return literal(text, "success");
    }

    static MutableComponent warning(String text) {
        return literal(text, "warning");
    }

    static MutableComponent error(String text) {
        return literal(text, "error");
    }

    static MutableComponent copied(String text) {
        return literal(text, "copied");
    }

    static MutableComponent added(String text) {
        return literal(text, "added");
    }

    static MutableComponent duplicate(String text) {
        return literal(text, "duplicate");
    }

    static MutableComponent itemName(ItemStack stack) {
        return stack.getHoverName().copy().withStyle(style -> style.withColor(color("value")));
    }

    static MutableComponent toolName() {
        return renderPlain(PpweCompatConfig.message("message.tool.name"), "prefix");
    }

    static MutableComponent toolLoreLine(String key) {
        return renderPlain(PpweCompatConfig.message(key), "muted");
    }

    private static Component prefixed(Component body) {
        String prefix = PpweCompatConfig.message("message.prefix").trim();
        if (prefix.isEmpty()) {
            return body;
        }
        return literal(prefix, "prefix")
            .append(literal(" › ", "separator"))
            .append(body);
    }

    private static MutableComponent render(String messageKey, String baseColorKey, Map<String, Component> replacements) {
        return renderTemplate(PpweCompatConfig.message(messageKey), baseColorKey, replacements);
    }

    private static MutableComponent renderPlain(String template, String baseColorKey) {
        return renderTemplate(template, baseColorKey, Map.of());
    }

    private static MutableComponent renderTemplate(String template, String baseColorKey, Map<String, Component> replacements) {
        MutableComponent result = Component.empty();
        int cursor = 0;
        while (cursor < template.length()) {
            int open = template.indexOf('{', cursor);
            if (open < 0) {
                result.append(literal(template.substring(cursor), baseColorKey));
                break;
            }
            if (open > cursor) {
                result.append(literal(template.substring(cursor, open), baseColorKey));
            }
            int close = template.indexOf('}', open + 1);
            if (close < 0) {
                result.append(literal(template.substring(open), baseColorKey));
                break;
            }

            String token = template.substring(open + 1, close);
            Component replacement = replacements.get(token);
            if (replacement != null) {
                result.append(replacement.copy());
            } else {
                result.append(literal(template.substring(open, close + 1), baseColorKey));
            }
            cursor = close + 1;
        }
        return result;
    }

    private static MutableComponent literal(String text, String colorKey) {
        return Component.literal(text).withStyle(style -> style.withColor(color(colorKey)));
    }

    private static TextColor color(String key) {
        String raw = PpweCompatConfig.color("color." + key);
        try {
            return TextColor.fromRgb(Integer.parseInt(raw.substring(1), 16));
        } catch (RuntimeException e) {
            return TextColor.fromRgb(0xFFFFFF);
        }
    }
}
