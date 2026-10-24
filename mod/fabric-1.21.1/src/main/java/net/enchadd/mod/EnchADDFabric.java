package net.enchadd.mod;

import net.fabricmc.api.ModInitializer;

public final class EnchADDFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EnchADDNourishBehavior.init();
        EnchADDFabricDelvesense.init();
        EnchADDFabricLastStand.init();
        EnchADDFabricFortitude.init();
        EnchADDFabricWard.init();
        EnchADDFabricBarrier.init();
        EnchADDFabricParry.init();
        EnchADDFabricHoldfast.init();
        EnchADDFabricPlayerStates.init();
        EnchADDFabricEvasion.init();
        EnchADDFabricStonewake.init();
        EnchADDFabricIrrigation.init();
        EnchADDFabricWater.init();
        EnchADDFabricProjectileStatus.init();
        EnchADDFabricVolley.init();
        EnchADDFabricMeleeEffects.init();
        EnchADDFabricImmolate.init();
        EnchADDFabricHemorrhage.init();
        EnchADDFabricOverwhelm.init();
        EnchADDFabricFrostbrand.init();
        EnchADDFabricWingguard.init();
        EnchADDFabricPanic.init();
        EnchADDFabricRicochet.init();
        // Airbag and Steadfast are applied in their LivingEntity mixins.
        EnchADDFabricBreakguard.init();
        EnchADDFabricSmelting.init();
        EnchADDFabricRefine.init();
        EnchADDFabricReplanting.init();
        EnchADDFabricTelepathy.init();
        EnchADDFabricRashness.init();
        EnchADDFabricToolInteractions.init();
        EnchADDFabricFurrow.init();
        System.out.println("[EnchADD] Fabric mod initialized: " + EnchADDModCommon.status());
    }
}
