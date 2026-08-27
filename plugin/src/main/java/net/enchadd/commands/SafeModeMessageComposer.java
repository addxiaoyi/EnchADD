package net.enchadd.commands;

import net.kyori.adventure.text.Component;

final class SafeModeMessageComposer {

    private SafeModeMessageComposer() {
    }

    static Component disabled() {
        return Component.text("[ENCHADD-SAFEMODE] 配置已禁用 safety-mode.enabled，当前不可开启。");
    }

    static Component usage() {
        return Component.text("[ENCHADD-SAFEMODE] 用法: /enchadd safemode [status|on|off|toggle]");
    }

    static Component status(String status) {
        return Component.text(status);
    }
}
