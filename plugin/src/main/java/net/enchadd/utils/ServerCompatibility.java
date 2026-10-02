package net.enchadd.utils;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects the running server version without binding the plugin to a version-specific implementation.
 */
public final class ServerCompatibility {

    public static final ServerVersion MINIMUM_LEGACY_VERSION = new ServerVersion(1, 21, 11);
    private static final Pattern VERSION_PATTERN = Pattern.compile("(?<!\\d)(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

    private ServerCompatibility() {
    }

    public static @NotNull Detection detect() {
        Server server;
        try {
            server = Bukkit.getServer();
        } catch (RuntimeException ex) {
            return Detection.unknown("Bukkit.getServer() failed: " + ex.getMessage());
        }
        if (server == null) {
            return Detection.unknown("Bukkit server is not initialized");
        }

        return detect(server.getMinecraftVersion(), server.getBukkitVersion(), server.getVersion());
    }

    static @NotNull Detection detect(@Nullable String minecraftVersion,
                                     @Nullable String bukkitVersion,
                                     @Nullable String serverVersion) {
        String[] candidates = {minecraftVersion, bukkitVersion, serverVersion};
        for (String candidate : candidates) {
            ServerVersion parsed = parse(candidate);
            if (parsed != null) {
                return new Detection(statusFor(parsed), parsed, candidate);
            }
        }
        return Detection.unknown("No parseable server version was reported");
    }

    static @Nullable ServerVersion parse(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        Matcher matcher = VERSION_PATTERN.matcher(value);
        if (!matcher.find()) {
            return null;
        }

        int major = parsePart(matcher.group(1));
        int minor = parsePart(matcher.group(2));
        int patch = matcher.group(3) == null ? 0 : parsePart(matcher.group(3));
        return new ServerVersion(major, minor, patch);
    }

    private static Status statusFor(@NotNull ServerVersion version) {
        if (version.major() == 1) {
            return version.compareTo(MINIMUM_LEGACY_VERSION) >= 0 ? Status.SUPPORTED : Status.UNSUPPORTED;
        }
        if (version.major() >= 26) {
            return Status.SUPPORTED;
        }
        return Status.UNSUPPORTED;
    }

    private static int parsePart(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE;
        }
    }

    public enum Status {
        SUPPORTED,
        UNSUPPORTED,
        UNKNOWN
    }

    public record Detection(@NotNull Status status,
                            @Nullable ServerVersion version,
                            @NotNull String source) {

        static @NotNull Detection unknown(@NotNull String source) {
            return new Detection(Status.UNKNOWN, null, source);
        }

        public boolean shouldStart() {
            return status != Status.UNSUPPORTED;
        }

        public @NotNull String mode() {
            if (version == null) {
                return "unknown-safe-fallback";
            }
            if (version.major() >= 26) {
                return "paper-modern-26+";
            }
            return "paper-1.21+";
        }

        public @NotNull String displayVersion() {
            return version == null ? "unknown" : version.toString();
        }
    }

    public record ServerVersion(int major, int minor, int patch) implements Comparable<ServerVersion> {

        @Override
        public int compareTo(@NotNull ServerVersion other) {
            int majorCompare = Integer.compare(major, other.major);
            if (majorCompare != 0) {
                return majorCompare;
            }
            int minorCompare = Integer.compare(minor, other.minor);
            return minorCompare != 0 ? minorCompare : Integer.compare(patch, other.patch);
        }

        @Override
        public @NotNull String toString() {
            return major + "." + minor + "." + patch;
        }
    }
}