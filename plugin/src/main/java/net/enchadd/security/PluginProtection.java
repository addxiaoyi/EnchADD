package net.enchadd.security;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Lightweight release hardening checks for the plugin artifact.
 */
public final class PluginProtection {

    private static final String MAIN_CLASS = "net.enchadd.EnchADD";
    private static final String BOOTSTRAP_CLASS = "net.enchadd.EnchADDBootstrap";
    private static final String ENV_EXPECTED_SHA256 = "ENCHADD_EXPECTED_SHA256";
    private static final String PROP_EXPECTED_SHA256 = "enchadd.expectedSha256";
    private static final String PROP_STRICT = "enchadd.protection.strict";
    private static final String PROP_ALLOW_EXPLODED = "enchadd.protection.allowExploded";
    private static final String PROP_ALLOW_INSTRUMENTATION = "enchadd.protection.allowInstrumentation";

    private PluginProtection() {
    }

    public static void verifyBootstrap(@NotNull Logger logger) {
        ProtectionReport report = inspect();
        report.logBootstrap(logger);
        if (report.blocked) {
            throw new IllegalStateException(report.blockReason);
        }
    }

    public static boolean verifyPlugin(@NotNull JavaPlugin plugin) {
        ProtectionReport report = inspect();
        report.validatePluginDescription(plugin.getDescription());
        report.logPlugin(plugin);
        if (report.blocked) {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
        return true;
    }

    private static ProtectionReport inspect() {
        ProtectionReport report = new ProtectionReport();
        Path artifactPath = resolveArtifactPath();
        report.artifactPath = artifactPath;

        if (artifactPath == null) {
            report.warn("无法定位插件代码来源，无法完成完整性校验。");
            return report;
        }

        if (Files.isDirectory(artifactPath)) {
            report.warn("插件正在以 classes 目录运行，发布环境请使用 protected-release jar。");
            if (strict() && !Boolean.getBoolean(PROP_ALLOW_EXPLODED)) {
                report.block("严格保护模式拒绝从展开目录启动插件。");
            }
            return report;
        }

        if (!artifactPath.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
            report.warn("插件代码来源不是 jar: " + artifactPath.getFileName());
            return report;
        }

        verifyJarShape(artifactPath, report);
        verifyChecksum(artifactPath, report);
        detectInstrumentation(report);
        return report;
    }

    private static void verifyJarShape(Path artifactPath, ProtectionReport report) {
        try (JarFile jar = new JarFile(artifactPath.toFile(), true)) {
            requireEntry(jar, "paper-plugin.yml", report);
            requireEntry(jar, "plugin.yml", report);
            requireEntry(jar, MAIN_CLASS.replace('.', '/') + ".class", report);
            requireEntry(jar, BOOTSTRAP_CLASS.replace('.', '/') + ".class", report);
            rejectEntry(jar, "net/enchadd/EnchADD.java", "jar 内包含源代码文件，疑似未使用发布包。", report);
            rejectEntry(jar, "META-INF/maven/net.enchadd/enchadd-plugin/pom.xml", "jar 内包含 Maven 坐标元数据，发布包应使用 protected-release profile 混淆。", report);
        } catch (IOException ex) {
            report.block("无法读取插件 jar: " + ex.getMessage());
        }
    }

    private static void requireEntry(JarFile jar, String name, ProtectionReport report) {
        JarEntry entry = jar.getJarEntry(name);
        if (entry == null) {
            report.block("插件 jar 缺少关键资源: " + name);
        }
    }

    private static void rejectEntry(JarFile jar, String name, String message, ProtectionReport report) {
        JarEntry entry = jar.getJarEntry(name);
        if (entry != null) {
            report.warn(message);
            if (strict()) {
                report.block(message);
            }
        }
    }

    private static void verifyChecksum(Path artifactPath, ProtectionReport report) {
        String expected = expectedSha256();
        if (expected.isBlank()) {
            report.warn("未配置 " + PROP_EXPECTED_SHA256 + " 或 " + ENV_EXPECTED_SHA256 + "，跳过发布包哈希绑定。");
            return;
        }
        if (!expected.matches("(?i)[0-9a-f]{64}")) {
            report.block("期望 SHA-256 格式无效。");
            return;
        }
        try {
            String actual = sha256(artifactPath);
            report.sha256 = actual;
            if (!actual.equalsIgnoreCase(expected)) {
                report.block("插件 jar 哈希不匹配，可能已被篡改。expected=" + expected + " actual=" + actual);
            }
        } catch (IOException | NoSuchAlgorithmException ex) {
            report.block("计算插件 jar 哈希失败: " + ex.getMessage());
        }
    }

    private static void detectInstrumentation(ProtectionReport report) {
        if (Boolean.getBoolean(PROP_ALLOW_INSTRUMENTATION)) {
            return;
        }
        List<String> arguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String argument : arguments) {
            String normalized = argument.toLowerCase(Locale.ROOT);
            if (normalized.contains("-javaagent")
                    || normalized.contains("-agentlib:jdwp")
                    || normalized.contains("-xdebug")
                    || normalized.contains("bytebuddy")
                    || normalized.contains("jrebel")) {
                String message = "检测到调试或字节码注入参数: " + argument;
                if (strict()) {
                    report.block(message);
                } else {
                    report.warn(message);
                }
            }
        }
    }

    private static Path resolveArtifactPath() {
        CodeSource codeSource = PluginProtection.class.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            return null;
        }
        URL location = codeSource.getLocation();
        if (location == null) {
            return null;
        }
        try {
            return Path.of(location.toURI()).toAbsolutePath().normalize();
        } catch (URISyntaxException | IllegalArgumentException ex) {
            return null;
        }
    }

    private static String expectedSha256() {
        String property = System.getProperty(PROP_EXPECTED_SHA256, "").trim();
        if (!property.isBlank()) {
            return property;
        }
        String env = System.getenv(ENV_EXPECTED_SHA256);
        return env == null ? "" : env.trim();
    }

    private static boolean strict() {
        return Boolean.parseBoolean(System.getProperty(PROP_STRICT, "true"));
    }

    private static String sha256(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[16384];
        try (InputStream input = Files.newInputStream(path)) {
            int read;
            while ((read = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static final class ProtectionReport {
        private Path artifactPath;
        private String sha256 = "";
        private boolean blocked;
        private String blockReason = "";
        private final StringBuilder warnings = new StringBuilder();

        private void validatePluginDescription(PluginDescriptionFile description) {
            if (!MAIN_CLASS.equals(description.getMain())) {
                block("plugin.yml main 被篡改: " + description.getMain());
            }
        }

        private void warn(String warning) {
            if (!warnings.isEmpty()) {
                warnings.append(" | ");
            }
            warnings.append(warning);
        }

        private void block(String reason) {
            blocked = true;
            blockReason = reason;
        }

        private void logBootstrap(Logger logger) {
            if (!warnings.isEmpty()) {
                logger.warn("[EnchADD Protection] {}", warnings);
            }
            if (blocked) {
                logger.error("[EnchADD Protection] {}", blockReason);
            }
        }

        private void logPlugin(JavaPlugin plugin) {
            if (artifactPath != null) {
                plugin.getLogger().info("[EnchADD Protection] artifact=" + artifactPath.getFileName()
                        + (sha256.isBlank() ? "" : " sha256=" + sha256));
            }
            if (!warnings.isEmpty()) {
                plugin.getLogger().warning("[EnchADD Protection] " + warnings);
            }
            if (blocked) {
                plugin.getLogger().severe("[EnchADD Protection] " + blockReason);
            }
        }
    }
}
