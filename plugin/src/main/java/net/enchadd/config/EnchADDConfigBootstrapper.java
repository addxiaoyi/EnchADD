package net.enchadd.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class EnchADDConfigBootstrapper {

    private EnchADDConfigBootstrapper() {
    }

    public static @NotNull FileConfiguration loadOrCreate(@NotNull Path filePath) throws IOException {
        File file = filePath.toFile();
        if (!file.exists()) {
            file.mkdirs();
        }

        File configFile = new File(filePath.toFile(), "config.yml");
        if (!configFile.exists()) {
            boolean created = configFile.createNewFile();
            if (!created) {
                throw new IOException("Failed to create config.yml");
            }
        }
        return YamlConfiguration.loadConfiguration(configFile);
    }

    public static void applyTopLevelDocumentation(@NotNull FileConfiguration configuration) {
        configuration.options().setHeader(List.of(
                "EnchADD 插件配置文件",
                "",
                "这是 EnchADD 插件的自动生成配置文件。",
                "你可以在这里配置每个附魔的基础属性、权重以及支持的物品。",
                "",
                "配置说明:",
                "- anvilCost: 在铁砧中应用该附魔的基础花费。",
                "- weight: 权重，决定了该附魔在附魔台或战利品箱中出现的概率。",
                "- minimumCost: 附魔所需的最小经验等级 (base: 基础, additionalPerLevel: 每级额外增加)。",
                "- maximumCost: 附魔所需的最大经验等级 (base: 基础, additionalPerLevel: 每级额外增加)。",
                "- enchantmentTags: 附魔标签，例如 #in_enchanting_table 表示可以从附魔台获得。",
                "- supportedItemTags: 支持该附魔的物品/标签列表。",
                "- enabled: 是否启用该附魔。"
        ));
        configuration.setComments("conflicts", List.of(
                "以下为默认互斥附魔组合，可按需增删。",
                "当 conflictPolicyVersion 升级时，缺失的默认冲突会自动补齐。",
                "无效键名、重复项和自身互斥项会在加载时自动清理。"
        ));
        configuration.setComments("safety-mode", List.of(
                "安全模式：出现告警峰值时自动降级，保留核心战斗逻辑。",
                "enabled: 是否启用安全模式总开关。",
                "auto-on-alert: 是否允许运行时告警自动触发安全模式。",
                "chance-multiplier: 安全模式下全局概率缩放系数(0-1)。",
                "tick-modulo-multiplier: 安全模式下周期逻辑降频倍数(>=1)。",
                "suppress-particles: 安全模式下是否直接关闭粒子队列提交。"
        ));
        configuration.setComments("enchant-budget", List.of(
                "附魔执行预算：每个附魔每 tick 的运行预算和熔断策略。",
                "nanos-per-tick: 单附魔每 tick 预算纳秒数。",
                "degraded-chance-multiplier: 超预算降级时的概率缩放(0-1)。",
                "degraded-tick-modulo-multiplier: 超预算降级时的周期降频倍数(>=1)。",
                "suppress-particles: 超预算降级时是否强制抑制粒子。",
                "breaker-consecutive-overruns: 连续超预算 tick 数达到阈值后开启熔断。",
                "breaker-cooldown-ticks: 熔断持续 tick 数。",
                "breaker-skip-execution: 熔断期间是否直接跳过监听执行（默认 false，保留核心战斗逻辑）。"
        ));
        configuration.setComments("update-checker", List.of(
                "GitHub Release 更新检查：只通知管理员，不会自动下载或替换运行中的 jar。",
                "enabled: 是否在插件启动时异步检查最新稳定版。",
                "repository: GitHub 仓库，格式为 owner/repository。",
                "timeout-seconds: GitHub API 请求超时秒数（2-30）。"
        ));
    }

    public static void save(@NotNull FileConfiguration configuration, @NotNull Path filePath) throws IOException {
        configuration.save(new File(filePath.toFile(), "config.yml"));
    }
}
