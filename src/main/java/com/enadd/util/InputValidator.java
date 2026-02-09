package com.enadd.util;

import java.util.regex.Pattern;


/**
 * 输入验证工具类 - 防止注入攻击和非法输入
 */
public final class InputValidator {

    private static final int MAX_INPUT_LENGTH = 256;
    private static final int MAX_COMMAND_LENGTH = 512;

    // 危险字符模式
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(['\";#]|--|/\\*|\\*/|\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|UNION)\\b)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern COMMAND_INJECTION_PATTERN = Pattern.compile(
        "[;&|`$(){}\\[\\]\\\\]",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern HTML_INJECTION_PATTERN = Pattern.compile(
        "<[^>]*>|javascript:|on\\w+\\s*=",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        "\\.\\./|\\.\\\\|/\\.\\./|\\\\.\\\\",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern VALID_PLAYER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
    private static final Pattern VALID_ENCHANTMENT_ID_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$");
    private static final Pattern VALID_UUID_PATTERN = Pattern.compile(
        "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
        Pattern.CASE_INSENSITIVE
    );

    private InputValidator() {}

    /**
     * 验证玩家名称
     */
    public static ValidationResult validatePlayerName(String name) {
        // Bug #478: 添加更详细的null检查
        if (name == null) {
            return ValidationResult.fail("玩家名称不能为null");
        }
        if (name.isEmpty()) {
            return ValidationResult.fail("玩家名称不能为空");
        }
        // Bug #479: 添加最小长度检查
        if (name.length() < 3) {
            return ValidationResult.fail("玩家名称长度不能少于3个字符");
        }
        if (name.length() > 16) {
            return ValidationResult.fail("玩家名称长度不能超过16个字符");
        }

        try {
            if (!VALID_PLAYER_NAME_PATTERN.matcher(name).matches()) {
                return ValidationResult.fail("玩家名称只能包含字母、数字和下划线，长度3-16位");
            }
        } catch (Exception e) {
            // Bug #480: 添加异常处理
            return ValidationResult.fail("验证玩家名称时出错: " + e.getMessage());
        }

        return ValidationResult.success();
    }

    /**
     * 验证附魔ID
     */
    public static ValidationResult validateEnchantmentId(String id) {
        // Bug #481: 添加更详细的null检查
        if (id == null) {
            return ValidationResult.fail("附魔ID不能为null");
        }
        if (id.isEmpty()) {
            return ValidationResult.fail("附魔ID不能为空");
        }
        // Bug #482: 添加最小长度检查
        if (id.length() < 2) {
            return ValidationResult.fail("附魔ID长度不能少于2个字符");
        }
        if (id.length() > MAX_INPUT_LENGTH) {
            return ValidationResult.fail("附魔ID长度超过限制（最大" + MAX_INPUT_LENGTH + "）");
        }

        try {
            if (!VALID_ENCHANTMENT_ID_PATTERN.matcher(id).matches()) {
                return ValidationResult.fail("附魔ID格式无效，只能包含小写字母、数字和下划线，且必须以字母开头");
            }
        } catch (Exception e) {
            // Bug #483: 添加异常处理
            return ValidationResult.fail("验证附魔ID时出错: " + e.getMessage());
        }

        return ValidationResult.success();
    }

    /**
     * 验证UUID
     */
    public static ValidationResult validateUUID(String uuid) {
        // Bug #484: 添加更详细的null检查
        if (uuid == null) {
            return ValidationResult.fail("UUID不能为null");
        }
        if (uuid.isEmpty()) {
            return ValidationResult.fail("UUID不能为空");
        }
        // Bug #485: 添加长度检查
        if (uuid.length() != 36) {
            return ValidationResult.fail("UUID长度必须为36个字符");
        }

        try {
            if (!VALID_UUID_PATTERN.matcher(uuid).matches()) {
                return ValidationResult.fail("UUID格式无效");
            }
        } catch (Exception e) {
            // Bug #486: 添加异常处理
            return ValidationResult.fail("验证UUID时出错: " + e.getMessage());
        }

        return ValidationResult.success();
    }

    /**
     * 验证普通文本输入
     */
    public static ValidationResult validateTextInput(String input, String fieldName) {
        // Bug #487: 验证fieldName参数
        if (fieldName == null || fieldName.isEmpty()) {
            fieldName = "输入";
        }

        if (input == null) {
            return ValidationResult.fail(fieldName + "不能为null");
        }

        // Bug #488: 允许空字符串但要明确说明
        if (input.isEmpty()) {
            return ValidationResult.success(); // 空字符串是有效的
        }

        if (input.length() > MAX_INPUT_LENGTH) {
            return ValidationResult.fail(fieldName + "长度不能超过" + MAX_INPUT_LENGTH + "个字符");
        }

        try {
            // 检查SQL注入
            if (SQL_INJECTION_PATTERN.matcher(input).find()) {
                return ValidationResult.fail(fieldName + "包含非法字符（SQL注入风险）");
            }

            // 检查命令注入
            if (COMMAND_INJECTION_PATTERN.matcher(input).find()) {
                return ValidationResult.fail(fieldName + "包含非法字符（命令注入风险）");
            }

            // 检查HTML注入
            if (HTML_INJECTION_PATTERN.matcher(input).find()) {
                return ValidationResult.fail(fieldName + "包含非法字符（XSS风险）");
            }

            // 检查路径遍历
            if (PATH_TRAVERSAL_PATTERN.matcher(input).find()) {
                return ValidationResult.fail(fieldName + "包含非法字符（路径遍历风险）");
            }
        } catch (Exception e) {
            // Bug #489: 添加异常处理
            return ValidationResult.fail("验证" + fieldName + "时出错: " + e.getMessage());
        }

        return ValidationResult.success();
    }

    /**
     * 验证命令参数
     */
    public static ValidationResult validateCommandArgs(String[] args) {
        // Bug #490: 添加null检查
        if (args == null) {
            return ValidationResult.success(); // null数组视为无参数
        }

        // Bug #491: 检查数组长度
        if (args.length == 0) {
            return ValidationResult.success(); // 空数组视为无参数
        }

        try {
            int totalLength = 0;
            for (String arg : args) {
                // Bug #492: 跳过null元素
                if (arg == null) continue;

                totalLength += arg.length();

                ValidationResult result = validateTextInput(arg, "命令参数");
                if (!result.isValid()) {
                    return result;
                }
            }

            if (totalLength > MAX_COMMAND_LENGTH) {
                return ValidationResult.fail("命令参数总长度不能超过" + MAX_COMMAND_LENGTH + "个字符");
            }
        } catch (Exception e) {
            // Bug #493: 添加异常处理
            return ValidationResult.fail("验证命令参数时出错: " + e.getMessage());
        }

        return ValidationResult.success();
    }

    /**
     * 验证数字范围
     */
    public static ValidationResult validateNumberRange(int value, int min, int max, String fieldName) {
        // Bug #494: 验证fieldName参数
        if (fieldName == null || fieldName.isEmpty()) {
            fieldName = "数值";
        }

        // Bug #495: 验证min和max的关系
        if (min > max) {
            return ValidationResult.fail("验证配置错误: 最小值大于最大值");
        }

        try {
            if (value < min || value > max) {
                return ValidationResult.fail(fieldName + "必须在 " + min + " 到 " + max + " 之间（当前值: " + value + "）");
            }
        } catch (Exception e) {
            // Bug #496: 添加异常处理
            return ValidationResult.fail("验证" + fieldName + "范围时出错: " + e.getMessage());
        }

        return ValidationResult.success();
    }

    /**
     * 清理输入字符串
     */
    public static String sanitizeInput(String input) {
        // Bug #497: 改进null处理
        if (input == null) {
            return "";
        }

        // Bug #498: 空字符串直接返回
        if (input.isEmpty()) {
            return input;
        }

        try {
            // 去除控制字符
            String sanitized = input.replaceAll("[\\x00-\\x1F\\x7F]", "");

            // Bug #499: 检查替换后是否为空
            if (sanitized.isEmpty()) {
                return "";
            }

            // 去除多余的空格
            sanitized = sanitized.trim().replaceAll("\\s+", " ");

            // HTML转义
            sanitized = sanitized
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");

            return sanitized;
        } catch (Exception e) {
            // Bug #500: 添加异常处理，返回空字符串
            return "";
        }
    }

    // Bug #501: 添加验证文件名的方法
    public static ValidationResult validateFileName(String fileName) {
        if (fileName == null) {
            return ValidationResult.fail("文件名不能为null");
        }
        if (fileName.isEmpty()) {
            return ValidationResult.fail("文件名不能为空");
        }
        if (fileName.length() > 255) {
            return ValidationResult.fail("文件名长度不能超过255个字符");
        }

        try {
            // 检查非法字符
            if (fileName.matches(".*[<>:\"/\\\\|?*].*")) {
                return ValidationResult.fail("文件名包含非法字符");
            }

            // 检查路径遍历
            if (PATH_TRAVERSAL_PATTERN.matcher(fileName).find()) {
                return ValidationResult.fail("文件名包含路径遍历字符");
            }

            // 检查保留名称（Windows）
            String upperName = fileName.toUpperCase();
            if (upperName.matches("^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\\..*)?$")) {
                return ValidationResult.fail("文件名使用了系统保留名称");
            }
        } catch (Exception e) {
            return ValidationResult.fail("验证文件名时出错: " + e.getMessage());
        }

        return ValidationResult.success();
    }

    // Bug #502: 添加验证整数的方法
    public static ValidationResult validateInteger(String value, String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            fieldName = "数值";
        }

        if (value == null || value.isEmpty()) {
            return ValidationResult.fail(fieldName + "不能为空");
        }

        try {
            Integer.parseInt(value);
            return ValidationResult.success();
        } catch (NumberFormatException e) {
            return ValidationResult.fail(fieldName + "必须是有效的整数");
        } catch (Exception e) {
            return ValidationResult.fail("验证" + fieldName + "时出错: " + e.getMessage());
        }
    }

    // Bug #503: 添加验证双精度浮点数的方法
    public static ValidationResult validateDouble(String value, String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            fieldName = "数值";
        }

        if (value == null || value.isEmpty()) {
            return ValidationResult.fail(fieldName + "不能为空");
        }

        try {
            Double.parseDouble(value);
            return ValidationResult.success();
        } catch (NumberFormatException e) {
            return ValidationResult.fail(fieldName + "必须是有效的数字");
        } catch (Exception e) {
            return ValidationResult.fail("验证" + fieldName + "时出错: " + e.getMessage());
        }
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            // Bug #504: 确保message不为null
            this.message = message != null ? message : "";
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String message) {
            // Bug #505: 验证message参数
            if (message == null || message.isEmpty()) {
                message = "验证失败";
            }
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        // Bug #506: 添加toString方法便于调试
        @Override
        public String toString() {
            return valid ? "ValidationResult[valid=true]" : "ValidationResult[valid=false, message=" + message + "]";
        }

        // Bug #507: 添加equals和hashCode方法
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ValidationResult that = (ValidationResult) obj;
            return valid == that.valid &&
                   (message == null ? that.message == null : message.equals(that.message));
        }

        @Override
        public int hashCode() {
            int result = Boolean.hashCode(valid);
            result = 31 * result + (message != null ? message.hashCode() : 0);
            return result;
        }
    }
}
