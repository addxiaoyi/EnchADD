package net.enchadd.commands;

final class CiStatusMetricFormatter {

    private CiStatusMetricFormatter() {
    }

    static String sanitizeMetricValue(String value) {
        if (value == null || value.isBlank()) {
            return "missing";
        }
        return value.replaceAll("\\s+", "_").replace('=', '-');
    }

    static String encodeMetricCodepoints(String value) {
        if (value == null || value.isBlank() || value.startsWith("EnchADD.enchant.") || value.startsWith("enchantment.")) {
            return "missing";
        }
        StringBuilder out = new StringBuilder();
        value.codePoints().forEach(codepoint -> {
            if (!out.isEmpty()) {
                out.append('-');
            }
            out.append(Integer.toHexString(codepoint));
        });
        return out.toString();
    }
}
