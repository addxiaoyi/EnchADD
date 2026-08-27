package net.enchadd;

import net.enchadd.utils.ComponentCache;
import net.enchadd.utils.EnchantPDC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 网络包验证测试 (Network Packet Validation Tests)
 *
 * 本测试类验证插件中所有涉及数据序列化/反序列化、类型转换
 * 以及跨边界数据传递的核心验证逻辑，模拟网络包处理场景。
 *
 * 测试范围:
 * - PersistentDataContainer 读写验证
 * - 位置序列化/反序列化验证
 * - UUID 序列化验证
 * - 组件缓存验证
 * - 字符串数据验证
 * - 数值类型边界验证
 */
public class NetworkPacketValidationTest {

    @Mock
    private World mockWorld;

    @Mock
    private Player mockPlayer;

    @Mock
    private PersistentDataHolder mockHolder;

    @BeforeEach
    void setUp() {
        Mockito.reset(mockWorld, mockPlayer, mockHolder);
    }

    // ==================== UUID 序列化验证 ====================

    @Nested
    @DisplayName("UUID 序列化/反序列化验证")
    class UuidSerializationValidation {

        @Test
        @DisplayName("UUID 应正确序列化为字符串并反序列化")
        void uuidShouldSerializeAndDeserializeCorrectly() {
            UUID original = UUID.randomUUID();

            EnchantPDC.setUUID(mockHolder, EnchantPDC.KEY_SOULBOUND_OWNER, original);
            UUID retrieved = EnchantPDC.getUUID(mockHolder, EnchantPDC.KEY_SOULBOUND_OWNER);

            assertEquals(original, retrieved, "UUID 反序列化后应与原值一致");
        }

        @Test
        @DisplayName("无效 UUID 字符串应返回 null")
        void invalidUuidStringShouldReturnNull() {
            // 模拟存储无效 UUID 的场景
            when(mockHolder.getPersistentDataContainer()
                    .get(EnchantPDC.KEY_SOULBOUND_OWNER, org.bukkit.persistence.PersistentDataType.STRING))
                    .thenReturn("invalid-uuid-string");

            UUID result = EnchantPDC.getUUID(mockHolder, EnchantPDC.KEY_SOULBOUND_OWNER);
            assertNull(result, "无效 UUID 字符串应返回 null");
        }

        @Test
        @DisplayName("不存在的 Key 应返回 null")
        void missingKeyShouldReturnNull() {
            UUID result = EnchantPDC.getUUID(mockHolder, EnchantPDC.KEY_SOULBOUND_OWNER);
            assertNull(result, "不存在的 Key 应返回 null");
        }

        @Test
        @DisplayName("特殊 UUID 应正确处理（零值 UUID）")
        void zeroUuidShouldBeHandledCorrectly() {
            UUID zeroUuid = new UUID(0L, 0L);

            EnchantPDC.setUUID(mockHolder, EnchantPDC.KEY_MARKED_TARGET, zeroUuid);
            UUID retrieved = EnchantPDC.getUUID(mockHolder, EnchantPDC.KEY_MARKED_TARGET);

            assertEquals(zeroUuid, retrieved, "零值 UUID 应正确处理");
        }

        @Test
        @DisplayName("最大 UUID 值应正确处理")
        void maxUuidShouldBeHandledCorrectly() {
            UUID maxUuid = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);

            EnchantPDC.setUUID(mockHolder, EnchantPDC.KEY_BLEED_LEVEL, maxUuid);
            UUID retrieved = EnchantPDC.getUUID(mockHolder, EnchantPDC.KEY_BLEED_LEVEL);

            assertEquals(maxUuid, retrieved, "最大 UUID 值应正确处理");
        }
    }

    // ==================== 位置序列化验证 ====================

    @Nested
    @DisplayName("位置序列化/反序列化验证")
    class LocationSerializationValidation {

        @Test
        @DisplayName("完整位置信息应正确序列化")
        void completeLocationShouldSerializeCorrectly() {
            when(mockWorld.getName()).thenReturn("world_nether");
            Location original = new Location(mockWorld, 100.5, 64.0, -200.75, 180.0f, 45.5f);

            String serialized = EnchantPDC.serializeLocation(original);
            assertNotNull(serialized, "序列化结果不应为空");
            assertTrue(serialized.contains("world_nether"), "应包含世界名称");
            assertTrue(serialized.contains("100.5"), "应包含 X 坐标");
            assertTrue(serialized.contains("64.0"), "应包含 Y 坐标");
            assertTrue(serialized.contains("-200.75"), "应包含 Z 坐标");
        }

        @Test
        @DisplayName("位置反序列化应还原完整数据")
        void locationDeserializationShouldRestoreCompleteData() {
            String serialized = "test_world,150.5,70.0,-300.25,90.0f,30.0f";

            Location deserialized = EnchantPDC.deserializeLocation(serialized);

            assertNotNull(deserialized, "反序列化结果不应为空");
            assertEquals(150.5, deserialized.getX(), 0.001, "X 坐标应一致");
            assertEquals(70.0, deserialized.getY(), 0.001, "Y 坐标应一致");
            assertEquals(-300.25, deserialized.getZ(), 0.001, "Z 坐标应一致");
            assertEquals(90.0f, deserialized.getYaw(), 0.001, "Yaw 应一致");
            assertEquals(30.0f, deserialized.getPitch(), 0.001, "Pitch 应一致");
        }

        @Test
        @DisplayName("null 位置应返回空字符串")
        void nullLocationShouldReturnEmptyString() {
            String result = EnchantPDC.serializeLocation(null);
            assertEquals("", result, "null 位置应返回空字符串");
        }

        @Test
        @DisplayName("无世界位置应返回空字符串")
        void locationWithoutWorldShouldReturnEmptyString() {
            Location locationWithoutWorld = new Location(null, 0, 0, 0);
            String result = EnchantPDC.serializeLocation(locationWithoutWorld);
            assertEquals("", result, "无世界的位置应返回空字符串");
        }

        @Test
        @DisplayName("无效序列化字符串应返回 null")
        void invalidSerializedStringShouldReturnNull() {
            assertNull(EnchantPDC.deserializeLocation(null), "null 输入应返回 null");
            assertNull(EnchantPDC.deserializeLocation(""), "空字符串应返回 null");
            assertNull(EnchantPDC.deserializeLocation("incomplete"), "不完整的坐标应返回 null");
            assertNull(EnchantPDC.deserializeLocation("world,invalid_x,not_number"), "无效数字应返回 null");
        }

        @Test
        @DisplayName("缺少可选字段的序列化应使用默认值")
        void missingOptionalFieldsShouldUseDefaults() {
            String serialized = "world_only,100,64,-50";

            Location deserialized = EnchantPDC.deserializeLocation(serialized);

            assertNotNull(deserialized, "即使缺少可选字段也应成功反序列化");
            assertEquals(100, deserialized.getX(), 0.001, "X 应正确");
            assertEquals(64, deserialized.getY(), 0.001, "Y 应正确");
            assertEquals(-50, deserialized.getZ(), 0.001, "Z 应正确");
            assertEquals(0f, deserialized.getYaw(), 0.001, "默认 Yaw 应为 0");
            assertEquals(0f, deserialized.getPitch(), 0.001, "默认 Pitch 应为 0");
        }

        @Test
        @DisplayName("负坐标应正确处理")
        void negativeCoordinatesShouldBeHandledCorrectly() {
            String serialized = "world,-1000.5,-64.25,-2000.75";

            Location deserialized = EnchantPDC.deserializeLocation(serialized);

            assertEquals(-1000.5, deserialized.getX(), 0.001, "负 X 应正确");
            assertEquals(-64.25, deserialized.getY(), 0.001, "负 Y 应正确");
            assertEquals(-2000.75, deserialized.getZ(), 0.001, "负 Z 应正确");
        }

        @Test
        @DisplayName("小数坐标精度应保持")
        void decimalPrecisionShouldBePreserved() {
            String serialized = "world,0.123456789,255.987654321,-0.000000001";

            Location deserialized = EnchantPDC.deserializeLocation(serialized);

            assertEquals(0.123456789, deserialized.getX(), 0.0000001, "X 精度应保持");
            assertEquals(255.987654321, deserialized.getY(), 0.0000001, "Y 精度应保持");
            assertEquals(-0.000000001, deserialized.getZ(), 0.000000001, "Z 精度应保持");
        }
    }

    // ==================== 整型数据验证 ====================

    @Nested
    @DisplayName("整型数据读写验证")
    class IntegerDataValidation {

        @Test
        @DisplayName("整型数据应正确读写")
        void integerDataShouldReadWriteCorrectly() {
            EnchantPDC.setInt(mockHolder, EnchantPDC.KEY_BLEED_LEVEL, 42);
            int retrieved = EnchantPDC.getInt(mockHolder, EnchantPDC.KEY_BLEED_LEVEL, -1);

            assertEquals(42, retrieved, "整型数据应正确读取");
        }

        @Test
        @DisplayName("不存在 Key 应返回默认值")
        void missingKeyShouldReturnDefault() {
            int result = EnchantPDC.getInt(mockHolder, EnchantPDC.KEY_BLEED_LEVEL, 100);
            assertEquals(100, result, "不存在 Key 应返回默认值");
        }

        @Test
        @DisplayName("零值应正确处理")
        void zeroValueShouldBeHandledCorrectly() {
            EnchantPDC.setInt(mockHolder, EnchantPDC.KEY_ARMOR_SUNDER, 0);
            int retrieved = EnchantPDC.getInt(mockHolder, EnchantPDC.KEY_ARMOR_SUNDER, -1);

            assertEquals(0, retrieved, "零值应正确读写");
        }

        @Test
        @DisplayName("负数值应正确处理")
        void negativeValueShouldBeHandledCorrectly() {
            EnchantPDC.setInt(mockHolder, EnchantPDC.KEY_BLEED_LEVEL, -50);
            int retrieved = EnchantPDC.getInt(mockHolder, EnchantPDC.KEY_BLEED_LEVEL, 0);

            assertEquals(-50, retrieved, "负数值应正确读写");
        }

        @Test
        @DisplayName("边界值应正确处理")
        void boundaryValuesShouldBeHandledCorrectly() {
            EnchantPDC.setInt(mockHolder, EnchantPDC.KEY_ARMOR_SUNDER, Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE,
                    EnchantPDC.getInt(mockHolder, EnchantPDC.KEY_ARMOR_SUNDER, 0),
                    "Integer.MAX_VALUE 应正确处理");

            EnchantPDC.setInt(mockHolder, EnchantPDC.KEY_ARMOR_SUNDER, Integer.MIN_VALUE);
            assertEquals(Integer.MIN_VALUE,
                    EnchantPDC.getInt(mockHolder, EnchantPDC.KEY_ARMOR_SUNDER, 0),
                    "Integer.MIN_VALUE 应正确处理");
        }
    }

    // ==================== Long 数据验证 ====================

    @Nested
    @DisplayName("Long 数据读写验证")
    class LongDataValidation {

        @Test
        @DisplayName("Long 数据应正确读写")
        void longDataShouldReadWriteCorrectly() {
            long timestamp = System.currentTimeMillis();

            EnchantPDC.setLong(mockHolder, EnchantPDC.KEY_BIND_EXPIRES, timestamp);
            long retrieved = EnchantPDC.getLong(mockHolder, EnchantPDC.KEY_BIND_EXPIRES, 0);

            assertEquals(timestamp, retrieved, "Long 数据应正确读取");
        }

        @Test
        @DisplayName("时间戳应正确处理")
        void timestampShouldBeHandledCorrectly() {
            long now = System.currentTimeMillis();
            long oneHourLater = now + 3600000;

            EnchantPDC.setLong(mockHolder, EnchantPDC.KEY_BIND_EXPIRES, oneHourLater);
            long retrieved = EnchantPDC.getLong(mockHolder, EnchantPDC.KEY_BIND_EXPIRES, 0);

            assertTrue(retrieved > now, "过期时间戳应大于当前时间");
            assertEquals(oneHourLater, retrieved, "时间戳应正确存储");
        }
    }

    // ==================== 字符串数据验证 ====================

    @Nested
    @DisplayName("字符串数据读写验证")
    class StringDataValidation {

        @Test
        @DisplayName("字符串数据应正确读写")
        void stringDataShouldReadWriteCorrectly() {
            EnchantPDC.setString(mockHolder, EnchantPDC.KEY_SOULBOUND_OWNER, "test_value");
            String retrieved = EnchantPDC.getString(mockHolder, EnchantPDC.KEY_SOULBOUND_OWNER, "");

            assertEquals("test_value", retrieved, "字符串数据应正确读取");
        }

        @Test
        @DisplayName("不存在 Key 应返回默认值")
        void missingKeyShouldReturnDefault() {
            String result = EnchantPDC.getString(mockHolder, EnchantPDC.KEY_SOULBOUND_OWNER, "default");
            assertEquals("default", result, "不存在 Key 应返回默认值");
        }

        @Test
        @DisplayName("空字符串应正确处理")
        void emptyStringShouldBeHandledCorrectly() {
            EnchantPDC.setString(mockHolder, EnchantPDC.KEY_HOME_LOCATION, "");
            String retrieved = EnchantPDC.getString(mockHolder, EnchantPDC.KEY_HOME_LOCATION, "default");

            assertEquals("", retrieved, "空字符串应正确读写");
        }

        @Test
        @DisplayName("特殊字符应正确处理")
        void specialCharactersShouldBeHandledCorrectly() {
            String specialChars = "测试中文 & emoji @#$%^&*()_+-=[]{}|;':\",./<>?";
            org.bukkit.NamespacedKey key = EnchantPDC.KEY_HOME_LOCATION;

            EnchantPDC.setString(mockHolder, key, specialChars);
            String retrieved = EnchantPDC.getString(mockHolder, key, "");

            assertEquals(specialChars, retrieved, "特殊字符应正确处理");
        }

        @Test
        @DisplayName("hasString 应正确检测存在性")
        void hasStringShouldDetectExistence() {
            assertFalse(EnchantPDC.hasString(mockHolder, EnchantPDC.KEY_HOME_LOCATION),
                    "新 Key 不应存在");

            EnchantPDC.setString(mockHolder, EnchantPDC.KEY_HOME_LOCATION, "value");
            assertTrue(EnchantPDC.hasString(mockHolder, EnchantPDC.KEY_HOME_LOCATION),
                    "设置后 Key 应存在");
        }
    }

    // ==================== Component 缓存验证 ====================

    @Nested
    @DisplayName("组件缓存验证")
    class ComponentCacheValidation {

        @BeforeEach
        void clearCache() {
            ComponentCache.invalidate();
        }

        @Test
        @DisplayName("缓存应正确存储和返回组件")
        void cacheShouldStoreAndReturnComponents() {
            Component expected = Component.text("测试消息", NamedTextColor.RED);

            ComponentCache.register("test_key", expected);
            Component retrieved = ComponentCache.get("test_key");

            assertNotNull(retrieved, "已注册的组件应能正确获取");
            assertEquals(expected, retrieved, "获取的组件应与存储的一致");
        }

        @Test
        @DisplayName("不存在的 Key 应返回 null")
        void missingKeyShouldReturnNull() {
            Component result = ComponentCache.get("nonexistent_key");
            assertNull(result, "不存在的 Key 应返回 null");
        }

        @Test
        @DisplayName("getOrCreate 应创建并缓存组件")
        void getOrCreateShouldCreateAndCache() {
            Component result1 = ComponentCache.getOrCreateText("create_test", "测试文本");
            Component result2 = ComponentCache.getOrCreateText("create_test", "其他文本");

            assertNotNull(result1, "getOrCreate 应返回非空结果");
            assertEquals(result1, result2, "相同 Key 应返回缓存的同一实例");
            assertTrue(result2 != null && result2.toString().contains("测试文本"),
                    "内容应正确");
        }

        @Test
        @DisplayName("invalidate 应清空动态缓存")
        void invalidateShouldClearDynamicCache() {
            ComponentCache.register("temp_key", Component.text("临时"));
            assertNotNull(ComponentCache.get("temp_key"), "注册后应能获取");

            ComponentCache.invalidate();
            assertNull(ComponentCache.get("temp_key"), "invalidate 后应无法获取");
        }

        @Test
        @DisplayName("系统常量 PREFIX 应始终可用")
        void systemPrefixShouldAlwaysBeAvailable() {
            assertNotNull(ComponentCache.PREFIX, "PREFIX 常量不应为 null");
            assertFalse(ComponentCache.PREFIX.equals(Component.text("")),
                    "PREFIX 应包含内容");
        }

        @Test
        @DisplayName("cooldownRemaining 应生成正确格式")
        void cooldownRemainingShouldGenerateCorrectFormat() {
            Component result = ComponentCache.cooldownRemaining(5000);

            assertNotNull(result, "cooldownRemaining 应返回非空组件");
            assertTrue(result.toString().contains("5") || result.toString().contains("5.0"),
                    "应包含秒数信息");
        }

        @Test
        @DisplayName("exportDone 应正确格式化路径")
        void exportDoneShouldFormatPathCorrectly() {
            String testPath = "/plugins/EnchADD/exports/test.yml";
            Component result = ComponentCache.exportDone(testPath);

            assertNotNull(result, "exportDone 应返回非空组件");
            assertTrue(result.toString().contains(testPath) || result.toString().contains("test.yml"),
                    "应包含路径信息");
        }
    }

    // ==================== 数据一致性验证 ====================

    @Nested
    @DisplayName("跨模块数据一致性验证")
    class CrossModuleDataConsistency {

        @Test
        @DisplayName("序列化的位置应能被正确反序列化")
        void serializedLocationShouldDeserializeCorrectly() {
            when(mockWorld.getName()).thenReturn("world_the_end");
            Location original = new Location(mockWorld, 500.0, 100.0, -500.0, 45.0f, -30.0f);

            String serialized = EnchantPDC.serializeLocation(original);
            Location deserialized = EnchantPDC.deserializeLocation(serialized);

            assertNotNull(deserialized, "反序列化结果不应为空");
            assertEquals(original.getWorld().getName(), deserialized.getWorld().getName(),
                    "世界名称应一致");
            assertEquals(original.getX(), deserialized.getX(), 0.001, "X 应一致");
            assertEquals(original.getY(), deserialized.getY(), 0.001, "Y 应一致");
            assertEquals(original.getZ(), deserialized.getZ(), 0.001, "Z 应一致");
        }

        @Test
        @DisplayName("UUID 序列化往返应保持一致性")
        void uuidSerializationRoundTripShouldMaintainConsistency() {
            UUID original = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            EnchantPDC.setUUID(mockHolder, EnchantPDC.KEY_SOULBOUND_OWNER, original);
            UUID roundTrip = EnchantPDC.getUUID(mockHolder, EnchantPDC.KEY_SOULBOUND_OWNER);

            assertEquals(original, roundTrip, "往返序列化应保持一致");
            assertEquals(original.toString(), roundTrip.toString(),
                    "字符串表示应一致");
        }
    }

    // ==================== 边界条件验证 ====================

    @Nested
    @DisplayName("边界条件验证")
    class BoundaryConditionValidation {

        @Test
        @DisplayName("世界边界内的大坐标应保持精度")
        void largeNumberSerializationShouldPreserveWorldBoundedCoordinates() {
            double largeX = 29999984.0;
            double largeY = 500.0;
            double largeZ = -30000000.0;

            Location loc = new Location(mockWorld, largeX, largeY, largeZ);
            String serialized = EnchantPDC.serializeLocation(loc);
            Location deserialized = EnchantPDC.deserializeLocation(serialized);

            assertNotNull(deserialized, "极大数值应能正确序列化");
            assertEquals(largeX, deserialized.getX(), 0.001, "X 值应正确");
        }

        @Test
        @DisplayName("越过世界边界或包含非有限值的位置应被拒绝")
        void unsafeLocationPayloadsShouldBeRejected() {
            assertNull(EnchantPDC.deserializeLocation("world,30000000,64,0"));
            assertNull(EnchantPDC.deserializeLocation("world,NaN,64,0"));
            assertNull(EnchantPDC.deserializeLocation("world,0,64,Infinity"));
            assertNull(EnchantPDC.deserializeLocation("world,0,64,0,NaN,0"));
            assertNull(EnchantPDC.deserializeLocation("world,0,64,0,0,0,extra"));
            assertNull(EnchantPDC.deserializeLocation("x".repeat(513)));
        }

        @Test
        @DisplayName("极小数值序列化应保持精度")
        void tinyNumberSerializationShouldMaintainPrecision() {
            double tinyX = 0.000001;
            double tinyY = 0.0000001;
            double tinyZ = -0.000001;

            Location loc = new Location(mockWorld, tinyX, tinyY, tinyZ);
            String serialized = EnchantPDC.serializeLocation(loc);
            Location deserialized = EnchantPDC.deserializeLocation(serialized);

            assertNotNull(deserialized, "极小数值应能正确序列化");
            assertEquals(tinyX, deserialized.getX(), 0.0000001, "X 精度应保持");
        }

        @Test
        @DisplayName("Yaw/Pitch 边界值应正确处理")
        void yawPitchBoundaryValuesShouldBeHandled() {
            Location loc = new Location(mockWorld, 0, 64, 0, 360f, 180f);
            String serialized = EnchantPDC.serializeLocation(loc);
            Location deserialized = EnchantPDC.deserializeLocation(serialized);

            assertNotNull(deserialized, "带边界角度的位置应能正确序列化");
            assertEquals(360f, deserialized.getYaw(), 0.001, "Yaw 边界值应正确");
            assertEquals(180f, deserialized.getPitch(), 0.001, "Pitch 边界值应正确");
        }
    }
}
