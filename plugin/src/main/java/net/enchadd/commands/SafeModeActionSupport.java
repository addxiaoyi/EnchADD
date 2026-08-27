package net.enchadd.commands;

import net.enchadd.utils.SafetyModeManager;

import java.util.Locale;

final class SafeModeActionSupport {

    private SafeModeActionSupport() {
    }

    static boolean apply(String[] args) {
        String action = action(args);
        switch (action) {
            case "on" -> SafetyModeManager.setManualEnabled(true, "command_on");
            case "off" -> SafetyModeManager.setManualEnabled(false, "command_off");
            case "toggle" -> SafetyModeManager.setManualEnabled(!SafetyModeManager.isManualEnabled(), "command_toggle");
            case "status" -> {
                // status-only path
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    static String action(String[] args) {
        return args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "toggle";
    }
}
