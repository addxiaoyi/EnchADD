package com.enadd.util;

public final class ColorFormatter {

    private ColorFormatter() {}

    private static String formatColor(char code, String text) {
        // Bug #427: 添加text参数验证（虽然当前未使用，但为了一致性）
        if (text == null) {
            text = "";
        }

        String colorCode;
        try {
            // Bug #428: 使用switch替代大量if-else，提高性能和可读性
            switch (code) {
                case '0': colorCode = "#000000"; break;
                case '1': colorCode = "#0000AA"; break;
                case '2': colorCode = "#00AA00"; break;
                case '3': colorCode = "#00AAAA"; break;
                case '4': colorCode = "#AA0000"; break;
                case '5': colorCode = "#AA00AA"; break;
                case '6': colorCode = "#FFAA00"; break;
                case '7': colorCode = "#AAAAAA"; break;
                case '8': colorCode = "#555555"; break;
                case '9': colorCode = "#5555FF"; break;
                case 'a': case 'A': colorCode = "#55FF55"; break;
                case 'b': case 'B': colorCode = "#55FFFF"; break;
                case 'c': case 'C': colorCode = "#FF5555"; break;
                case 'd': case 'D': colorCode = "#FF55FF"; break;
                case 'e': case 'E': colorCode = "#FFFF55"; break;
                case 'f': case 'F': colorCode = "#FFFFFF"; break;
                case 'k': case 'K': colorCode = "§k"; break; // Bug #429: 修复obfuscated格式
                case 'l': case 'L': colorCode = "§l"; break;
                case 'm': case 'M': colorCode = "§m"; break;
                case 'n': case 'N': colorCode = "§n"; break;
                case 'o': case 'O': colorCode = "§o"; break;
                case 'r': case 'R': colorCode = "§r"; break;
                default: colorCode = ""; break;
            }
        } catch (Exception e) {
            // Bug #430: 添加异常处理
            colorCode = "";
        }

        // Bug #431: 修复逻辑错误 - 原代码在递归调用中有问题
        return colorCode;
    }

    public static String format(String text) {
        // Bug #432: 改进null检查
        if (text == null) {
            return "";
        }

        // Bug #433: 空字符串直接返回
        if (text.isEmpty()) {
            return text;
        }

        try {
            StringBuilder result = new StringBuilder(text.length() + 16); // Bug #434: 预分配容量
            int length = text.length();
            int i = 0;

            while (i < length) {
                // Bug #435: 添加边界检查
                if (i >= length) {
                    break;
                }

                char c = text.charAt(i);

                // Bug #436: 处理 & 颜色代码
                if (c == '&' && i + 1 < length) {
                    char code = text.charAt(i + 1);
                    String replacement = formatColor(code, "");

                    // Bug #437: 检查replacement是否为null
                    if (replacement != null && !replacement.isEmpty()) {
                        // Bug #438: 修复hex颜色代码转换逻辑
                        if (replacement.startsWith("#") && replacement.length() == 7) {
                            // 转换为Minecraft的hex格式: §x§R§R§G§G§B§B
                            result.append("§x");
                            String hex = replacement.substring(1); // 移除#
                            for (char h : hex.toCharArray()) {
                                result.append("§").append(h);
                            }
                        } else {
                            // 格式化代码（§l, §k等）
                            result.append(replacement);
                        }
                    } else {
                        // Bug #439: 无效代码时保留原字符
                        result.append(c);
                        if (i + 1 < length) {
                            result.append(code);
                        }
                    }
                    i += 2;
                }
                // Bug #440: 保留已有的§代码
                else if (c == '§' && i + 1 < length) {
                    result.append(c);
                    result.append(text.charAt(i + 1));
                    i += 2;
                }
                else {
                    result.append(c);
                    i++;
                }
            }

            return result.toString();
        } catch (Exception e) {
            // Bug #441: 添加异常处理，返回原文本
            return text;
        }
    }

    public static String colorize(String text) {
        // Bug #442: 添加null检查
        if (text == null) {
            return "";
        }

        try {
            return format(text);
        } catch (Exception e) {
            // Bug #443: 添加异常处理
            return text;
        }
    }

    // Bug #444: 添加strip方法，移除颜色代码
    public static String stripColor(String text) {
        if (text == null || text.isEmpty()) {
            return text != null ? text : "";
        }

        try {
            // 移除所有§和&颜色代码
            return text.replaceAll("§[0-9a-fk-or]", "")
                      .replaceAll("&[0-9a-fk-or]", "");
        } catch (Exception e) {
            return text;
        }
    }

    // Bug #445: 添加验证方法
    public static boolean isValidColorCode(char code) {
        try {
            String result = formatColor(code, "");
            return result != null && !result.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // Bug #446: 添加hex颜色验证
    public static boolean isValidHexColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return false;
        }

        try {
            // 检查格式: #RRGGBB
            if (hex.length() != 7 || hex.charAt(0) != '#') {
                return false;
            }

            // 验证hex字符
            String hexPart = hex.substring(1);
            for (char c : hexPart.toCharArray()) {
                if (!Character.isDigit(c) &&
                    (c < 'a' || c > 'f') &&
                    (c < 'A' || c > 'F')) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
