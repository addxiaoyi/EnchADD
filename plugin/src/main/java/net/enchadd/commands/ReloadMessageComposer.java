package net.enchadd.commands;

import net.kyori.adventure.text.Component;

final class ReloadMessageComposer {

    private ReloadMessageComposer() {
    }

    static Component failure() {
        return Component.text("[ENCHADD-RELOAD] 配置重载失败，请检查控制台日志。");
    }

    static Component success(String status) {
        return Component.text(status);
    }
}
