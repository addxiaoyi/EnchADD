package net.enchadd.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * 优化8：统一 PersistentDataContainer 工具类（Custom Data Holder）
 *
 * 为需要在实体/物品/块上存储自定义数据的附魔提供类型安全的统一访问入口：
 * - 灵魂绑定：在物品 PDC 记录原始拥有者 UUID
 * - 归家：在玩家 PDC 记录绑定的家位置（序列化为字符串）
 * - 陷阱/持续效果：在实体 PDC 记录附魔施加原与等级
 *
 * 使用 Paper 原生 PersistentDataContainer，比直接操作 NBT 更安全、更版本兼容。
 *
 * Key 命名规范：enchadd:<purpose>
 */
public class EnchantPDC {

    private static final int MAX_LOCATION_PAYLOAD_LENGTH = 512;
    private static final double WORLD_COORDINATE_LIMIT = 29_999_984D;

    // ─── 预定义 Key 常量（避免运行时重复创建 NamespacedKey 对象）────────────────────
    public static final NamespacedKey KEY_SOULBOUND_OWNER = ns("soulbound_owner");
    public static final NamespacedKey KEY_HOME_LOCATION   = ns("home_location");
    public static final NamespacedKey KEY_MARKED_TARGET   = ns("marked_target");
    public static final NamespacedKey KEY_BLEED_LEVEL     = ns("bleed_level");
    public static final NamespacedKey KEY_ARMOR_SUNDER    = ns("armor_sunder");
    public static final NamespacedKey KEY_BIND_EXPIRES    = ns("bind_expires");

    private EnchantPDC() {}

    private static NamespacedKey ns(String value) {
        return PerformanceUtils.enchaddKey(value);
    }

    // ─── 字符串数据 ──────────────────────────────────────────────────────────────

    public static void setString(PersistentDataHolder holder, NamespacedKey key, String value) {
        holder.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
    }

    public static String getString(PersistentDataHolder holder, NamespacedKey key, String def) {
        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        String v = pdc.get(key, PersistentDataType.STRING);
        return v != null ? v : def;
    }

    public static boolean hasString(PersistentDataHolder holder, NamespacedKey key) {
        return holder.getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    // ─── 整型数据 ────────────────────────────────────────────────────────────────

    public static void setInt(PersistentDataHolder holder, NamespacedKey key, int value) {
        holder.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, value);
    }

    public static int getInt(PersistentDataHolder holder, NamespacedKey key, int def) {
        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        Integer v = pdc.get(key, PersistentDataType.INTEGER);
        return v != null ? v : def;
    }

    // ─── Long 数据 ───────────────────────────────────────────────────────────────

    public static void setLong(PersistentDataHolder holder, NamespacedKey key, long value) {
        holder.getPersistentDataContainer().set(key, PersistentDataType.LONG, value);
    }

    public static long getLong(PersistentDataHolder holder, NamespacedKey key, long def) {
        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        Long v = pdc.get(key, PersistentDataType.LONG);
        return v != null ? v : def;
    }

    // ─── UUID（存为 String）───────────────────────────────────────────────────────

    public static void setUUID(PersistentDataHolder holder, NamespacedKey key, UUID uuid) {
        setString(holder, key, uuid.toString());
    }

    public static UUID getUUID(PersistentDataHolder holder, NamespacedKey key) {
        String s = getString(holder, key, null);
        if (s == null) return null;
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ─── 通用删除 ────────────────────────────────────────────────────────────────

    public static void remove(PersistentDataHolder holder, NamespacedKey key) {
        holder.getPersistentDataContainer().remove(key);
    }

    // ─── 位置序列化助手（用于归家附魔）──────────────────────────────────────────────

    /**
     * 将位置序列化为字符串 "world,x,y,z,yaw,pitch"
     */
    public static String serializeLocation(org.bukkit.Location loc) {
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," +
               loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    /**
     * 从字符串还原 Location；如果反序列化失败，返回 null。
     */
    public static org.bukkit.Location deserializeLocation(String s) {
        if (s == null || s.isBlank() || s.length() > MAX_LOCATION_PAYLOAD_LENGTH) return null;
        try {
            String[] parts = s.split(",", -1);
            if (parts.length < 4 || parts.length > 6) return null;
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(parts[0]);
            if (world == null) return null;
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0f;
            if (!isSafeLocation(x, y, z, yaw, pitch)) return null;
            return new org.bukkit.Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isSafeLocation(double x, double y, double z, float yaw, float pitch) {
        return Double.isFinite(x)
                && Double.isFinite(y)
                && Double.isFinite(z)
                && Float.isFinite(yaw)
                && Float.isFinite(pitch)
                && Math.abs(x) <= WORLD_COORDINATE_LIMIT
                && Math.abs(z) <= WORLD_COORDINATE_LIMIT;
    }
}
