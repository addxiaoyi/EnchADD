package net.enchadd.commands;

record EnchantListPerfRequest(int triggerCount, int particleCount) {

    static EnchantListPerfRequest from(String[] args) {
        int triggerCount = clamp(parseInt(args.length > 1 ? args[1] : "1200", 1200), 1, 200_000);
        int particleCount = clamp(parseInt(args.length > 2 ? args[2] : "600", 600), 0, 100_000);
        return new EnchantListPerfRequest(triggerCount, particleCount);
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
