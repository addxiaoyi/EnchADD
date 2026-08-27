package net.enchadd.commands;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.function.Function;

final class VerifyService {

    private VerifyService() {
    }

    static String run(JavaPlugin plugin,
                      Function<String, String> resolveCiEnchantName,
                      Function<String, Boolean> translationReadyForVerify) {
        VerifySupport.VerifyTally tally = new VerifySupport.VerifyTally(new ArrayList<>());

        VerifyConflictRules.run(tally);
        VerifyBoundaryRules.run(tally, translationReadyForVerify);
        VerifyDamageRules.run(tally);

        String message = VerifyMessageBuilder.build(tally);
        plugin.getLogger().info(message);
        return message;
    }
}
