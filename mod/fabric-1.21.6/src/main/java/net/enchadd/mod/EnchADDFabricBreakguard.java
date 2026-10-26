package net.enchadd.mod;

final class EnchADDFabricBreakguard {
    private EnchADDFabricBreakguard() {
    }

    static void init() {
        // Fabric uses BreakguardMixin because ALLOW_DAMAGE cannot mutate the incoming amount.
    }
}
