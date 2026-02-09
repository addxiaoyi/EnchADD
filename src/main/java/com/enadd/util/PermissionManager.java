package com.enadd.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 权限管理工具类 - 统一管理权限检查和防滥用机制
 */
public class PermissionManager {

    private static final class Holder {
        private static final PermissionManager INSTANCE = new PermissionManager();
    }

    private final Map<String, PermissionNode> permissionNodes = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> commandUsageCount = new ConcurrentHashMap<>();
    private final Map<String, Long> lastCommandTime = new ConcurrentHashMap<>();

    // 默认配置
    private volatile int maxCommandsPerMinute = 30;
    private volatile long commandCooldownMs = 1000; // 1秒
    private volatile boolean enableRateLimit = true;
    private volatile boolean enablePermissionCache = true;

    private PermissionManager() {
        initializeDefaultPermissions();
    }

    public static PermissionManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 初始化默认权限节点
     */
    private void initializeDefaultPermissions() {
        // 管理员权限
        registerPermission("enchadd.admin", PermissionLevel.ADMIN, "管理员权限");
        registerPermission("enchadd.admin.reload", PermissionLevel.ADMIN, "重载配置权限");
        registerPermission("enchadd.admin.debug", PermissionLevel.ADMIN, "调试模式权限");
        registerPermission("enchadd.admin.bypass", PermissionLevel.ADMIN, "绕过限制权限");

        // 用户权限
        registerPermission("enchadd.user", PermissionLevel.USER, "用户基本权限");
        registerPermission("enchadd.user.list", PermissionLevel.USER, "查看附魔列表权限");
        registerPermission("enchadd.user.info", PermissionLevel.USER, "查看附魔详情权限");
        registerPermission("enchadd.user.search", PermissionLevel.USER, "搜索附魔权限");

        // 特殊权限
        registerPermission("enchadd.enchant.use", PermissionLevel.USER, "使用附魔权限");
        registerPermission("enchadd.enchant.apply", PermissionLevel.USER, "应用附魔权限");
        registerPermission("enchadd.enchant.remove", PermissionLevel.USER, "移除附魔权限");

        // 成就系统权限
        registerPermission("enchadd.achievement.view", PermissionLevel.USER, "查看成就权限");
        registerPermission("enchadd.achievement.admin", PermissionLevel.ADMIN, "管理成就权限");

        // Web界面权限
        registerPermission("enchadd.web.access", PermissionLevel.USER, "访问Web界面权限");
        registerPermission("enchadd.web.admin", PermissionLevel.ADMIN, "Web管理权限");
    }

    /**
     * 注册权限节点
     */
    public void registerPermission(String node, PermissionLevel level, String description) {
        permissionNodes.put(node.toLowerCase(), new PermissionNode(node, level, description));
    }

    /**
     * 检查权限（带速率限制）
     */
    public PermissionCheckResult checkPermission(CommandSender sender, String permission) {
        // 控制台始终有权限
        if (!(sender instanceof Player)) {
            return PermissionCheckResult.granted();
        }

        Player player = (Player) sender;
        String playerId = player.getUniqueId().toString();

        // 检查速率限制
        if (enableRateLimit && !hasPermission(player, "enchadd.admin.bypass")) {
            PermissionCheckResult rateLimitResult = checkRateLimit(playerId);
            if (!rateLimitResult.isGranted()) {
                return rateLimitResult;
            }
        }

        // 检查权限
        if (!hasPermission(player, permission)) {
            return PermissionCheckResult.denied("你没有权限执行此操作");
        }

        // 记录命令使用
        recordCommandUsage(playerId);

        return PermissionCheckResult.granted();
    }

    /**
     * 检查玩家是否有权限
     */
    public boolean hasPermission(Player player, String permission) {
        if (player == null || permission == null) {
            return false;
        }

        // OP玩家拥有所有权限
        if (player.isOp()) {
            return true;
        }

        // 检查具体权限
        if (player.hasPermission(permission)) {
            return true;
        }

        // 检查通配符权限
        String[] parts = permission.split("\\.");
        StringBuilder wildcard = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            wildcard.append(parts[i]).append(".");
            if (player.hasPermission(wildcard + "*")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查速率限制
     */
    private PermissionCheckResult checkRateLimit(String playerId) {
        long currentTime = System.currentTimeMillis();

        // 检查命令间隔
        Long lastTime = lastCommandTime.get(playerId);
        if (lastTime != null) {
            long timeDiff = currentTime - lastTime;
            if (timeDiff < commandCooldownMs) {
                long waitTime = (commandCooldownMs - timeDiff) / 1000;
                return PermissionCheckResult.denied("请等待 " + waitTime + " 秒后再执行命令");
            }
        }

        // 检查每分钟命令数
        AtomicInteger count = commandUsageCount.get(playerId);
        if (count != null && count.get() >= maxCommandsPerMinute) {
            return PermissionCheckResult.denied("你已达到每分钟命令使用上限，请稍后再试");
        }

        return PermissionCheckResult.granted();
    }

    /**
     * 记录命令使用
     */
    private void recordCommandUsage(String playerId) {
        lastCommandTime.put(playerId, System.currentTimeMillis());
        commandUsageCount.computeIfAbsent(playerId, k -> new AtomicInteger(0)).incrementAndGet();
    }

    /**
     * 清理过期的速率限制记录
     */
    public void cleanupRateLimits() {
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60000;

        lastCommandTime.entrySet().removeIf(entry -> entry.getValue() < oneMinuteAgo);
        commandUsageCount.entrySet().removeIf(entry -> !lastCommandTime.containsKey(entry.getKey()));
    }

    /**
     * 获取权限节点信息
     */
    public PermissionNode getPermissionNode(String permission) {
        return permissionNodes.get(permission.toLowerCase());
    }

    /**
     * 获取所有权限节点
     */
    public Map<String, PermissionNode> getAllPermissions() {
        return new HashMap<>(permissionNodes);
    }

    /**
     * 设置速率限制配置
     */
    public void setRateLimitConfig(int maxPerMinute, long cooldownMs) {
        this.maxCommandsPerMinute = maxPerMinute;
        this.commandCooldownMs = cooldownMs;
    }

    /**
     * 启用/禁用速率限制
     */
    public void setRateLimitEnabled(boolean enabled) {
        this.enableRateLimit = enabled;
    }

    /**
     * 权限节点类
     */
    public static class PermissionNode {
        private final String node;
        private final PermissionLevel level;
        private final String description;

        public PermissionNode(String node, PermissionLevel level, String description) {
            this.node = node;
            this.level = level;
            this.description = description;
        }

        public String getNode() { return node; }
        public PermissionLevel getLevel() { return level; }
        public String getDescription() { return description; }
    }

    /**
     * 权限级别枚举
     */
    public enum PermissionLevel {
        ADMIN(100),   // 管理员
        MODERATOR(50), // 版主
        USER(10),      // 普通用户
        GUEST(0);      // 访客

        private final int priority;

        PermissionLevel(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    /**
     * 权限检查结果类
     */
    public static class PermissionCheckResult {
        private final boolean granted;
        private final String message;

        private PermissionCheckResult(boolean granted, String message) {
            this.granted = granted;
            this.message = message;
        }

        public static PermissionCheckResult granted() {
            return new PermissionCheckResult(true, null);
        }

        public static PermissionCheckResult denied(String message) {
            return new PermissionCheckResult(false, message);
        }

        public boolean isGranted() {
            return granted;
        }

        public String getMessage() {
            return message;
        }
    }
}
