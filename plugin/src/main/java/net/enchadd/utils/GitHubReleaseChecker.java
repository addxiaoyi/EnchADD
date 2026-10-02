package net.enchadd.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.enchadd.config.ConfigSupport;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/** Queries GitHub releases and optionally stages a verified jar for the next server restart. */
public final class GitHubReleaseChecker {

    private static final Pattern REPOSITORY_PATTERN = Pattern.compile("[A-Za-z0-9_.-]{1,100}/[A-Za-z0-9_.-]{1,100}");
    private static final Pattern ASSET_NAME_PATTERN = Pattern.compile("enchadd-plugin-[A-Za-z0-9._-]+-protected\\.jar");
    private static final int MIN_TIMEOUT_SECONDS = 2;
    private static final int MAX_TIMEOUT_SECONDS = 30;
    private static final int MAX_RESPONSE_BYTES = 64 * 1024;
    private static final int MAX_UPDATE_BYTES = 32 * 1024 * 1024;
    private static final int DEFAULT_INTERVAL_HOURS = 12;
    private static final int MAX_INTERVAL_HOURS = 168;
    private static final long INITIAL_DELAY_TICKS = 20L * 30L;
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(MAX_TIMEOUT_SECONDS))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private BukkitTask task;
    private String etag;

    public void start(@NotNull JavaPlugin plugin) {
        Settings settings = Settings.from(plugin);
        if (!settings.enabled()) {
            stop();
            return;
        }
        stop();
        long intervalTicks = settings.intervalHours() * 20L * 60L * 60L;
        task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin, () -> check(plugin, settings), INITIAL_DELAY_TICKS, intervalTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        etag = null;
    }

    private void check(JavaPlugin plugin, Settings settings) {
        try {
            Optional<Release> release = fetch(settings);
            if (release.isEmpty()) {
                return;
            }
            String current = plugin.getPluginMeta().getVersion();
            Release latest = release.get();
            if (!GitHubReleaseVersionSupport.isNewer(latest.version(), current)) {
                plugin.getLogger().info("[EnchADD Update] Current version " + current + " is up to date.");
                return;
            }

            plugin.getLogger().warning("[EnchADD Update] New version " + latest.version()
                    + " is available (current " + current + "): " + latest.url());
            if (settings.autoDownload()) {
                stageVerifiedUpdate(plugin, latest, settings);
            }
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            plugin.getLogger().warning("[EnchADD Update] GitHub update check failed: " + ex.getMessage());
        } catch (RuntimeException ex) {
            plugin.getLogger().warning("[EnchADD Update] GitHub response rejected: " + ex.getMessage());
        }
    }

    private Optional<Release> fetch(Settings settings) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(settings.apiUri())
                .timeout(Duration.ofSeconds(settings.timeoutSeconds()))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "EnchADD-Update-Checker")
                .GET();
        if (etag != null && !etag.isBlank()) {
            requestBuilder.header("If-None-Match", etag);
        }
        HttpResponse<String> response = CLIENT.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 304 || response.statusCode() == 404) {
            return Optional.empty();
        }
        if (response.statusCode() != 200) {
            throw new IOException("GitHub API returned HTTP " + response.statusCode());
        }
        response.headers().firstValue("ETag").ifPresent(value -> etag = value);
        String body = response.body();
        if (body.length() > MAX_RESPONSE_BYTES) {
            throw new IOException("GitHub API response exceeded " + MAX_RESPONSE_BYTES + " characters");
        }
        return parseRelease(body);
    }

    private void stageVerifiedUpdate(JavaPlugin plugin, Release release, Settings settings)
            throws IOException, InterruptedException {
        Asset asset = release.pluginAsset();
        if (asset == null) {
            plugin.getLogger().warning("[EnchADD Update] Release has no protected plugin jar asset; download skipped.");
            return;
        }
        String expectedDigest = normalizeDigest(asset.digest());
        if (expectedDigest.isBlank()) {
            plugin.getLogger().warning("[EnchADD Update] Release asset has no SHA-256 digest; download skipped.");
            return;
        }

        byte[] payload = download(asset.downloadUri(), settings.timeoutSeconds());
        String actualDigest = sha256(payload);
        if (!actualDigest.equalsIgnoreCase(expectedDigest)) {
            throw new IOException("downloaded update SHA-256 mismatch");
        }

        Path updateDirectory = plugin.getServer().getUpdateFolderFile().toPath().toAbsolutePath().normalize();
        Files.createDirectories(updateDirectory);
        Path target = updateDirectory.resolve(asset.name()).normalize();
        if (!target.startsWith(updateDirectory) || !ASSET_NAME_PATTERN.matcher(asset.name()).matches()) {
            throw new IOException("unsafe update asset name");
        }
        Path partial = updateDirectory.resolve(asset.name() + ".part");
        Files.write(partial, payload, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        try {
            moveIntoUpdateDirectory(partial, target);
        } finally {
            Files.deleteIfExists(partial);
        }
        plugin.getLogger().info("[EnchADD Update] Verified " + asset.name()
                + " and staged it in the Paper update directory. Restart the server to apply it.");
    }

    private static byte[] download(URI uri, int timeoutSeconds) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Accept", "application/octet-stream")
                .header("User-Agent", "EnchADD-Update-Checker")
                .GET()
                .build();
        HttpResponse<byte[]> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new IOException("GitHub asset returned HTTP " + response.statusCode());
        }
        byte[] payload = response.body();
        if (payload.length == 0 || payload.length > MAX_UPDATE_BYTES) {
            throw new IOException("update asset size is outside the safe limit");
        }
        return payload;
    }

    private static void moveIntoUpdateDirectory(Path partial, Path target) throws IOException {
        try {
            Files.move(partial, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static Optional<Release> parseRelease(@NotNull String body) {
        JsonElement root = JsonParser.parseString(body);
        if (!root.isJsonObject()) {
            return Optional.empty();
        }
        JsonObject release = root.getAsJsonObject();
        if (release.has("prerelease") && release.get("prerelease").getAsBoolean()) {
            return Optional.empty();
        }
        String tag = stringValue(release, "tag_name");
        String url = stringValue(release, "html_url");
        if (tag.isBlank() || !url.startsWith("https://github.com/")) {
            return Optional.empty();
        }

        List<Asset> assets = new ArrayList<>();
        JsonArray assetArray = release.getAsJsonArray("assets");
        if (assetArray != null) {
            for (JsonElement element : assetArray) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject asset = element.getAsJsonObject();
                String name = stringValue(asset, "name");
                String downloadUrl = stringValue(asset, "browser_download_url");
                if (!ASSET_NAME_PATTERN.matcher(name).matches() || !downloadUrl.startsWith("https://github.com/")) {
                    continue;
                }
                assets.add(new Asset(name, URI.create(downloadUrl), stringValue(asset, "digest")));
            }
        }
        return Optional.of(new Release(tag, url, List.copyOf(assets)));
    }

    private static String normalizeDigest(String digest) {
        if (digest == null) {
            return "";
        }
        String normalized = digest.trim();
        if (normalized.regionMatches(true, 0, "sha256:", 0, 7)) {
            normalized = normalized.substring(7);
        }
        return normalized.matches("(?i)[0-9a-f]{64}") ? normalized : "";
    }

    private static String sha256(byte[] payload) throws IOException {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(payload));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IOException("SHA-256 is unavailable", ex);
        }
    }

    private static String stringValue(JsonObject object, String member) {
        JsonElement value = object.get(member);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                ? value.getAsString().trim()
                : "";
    }

    record Release(@NotNull String version, @NotNull String url, @NotNull List<Asset> assets) {
        Asset pluginAsset() {
            String normalizedVersion = version.startsWith("v") ? version.substring(1) : version;
            String expectedName = "enchadd-plugin-" + normalizedVersion + "-protected.jar";
            return assets.stream().filter(asset -> asset.name().equals(expectedName)).findFirst().orElse(null);
        }
    }

    record Asset(@NotNull String name, @NotNull URI downloadUri, @NotNull String digest) {
    }

    private record Settings(boolean enabled,
                            @NotNull URI apiUri,
                            int timeoutSeconds,
                            int intervalHours,
                            boolean autoDownload) {
        static Settings from(JavaPlugin plugin) {
            ConfigurationSection section = ConfigSupport.getConfigSection(plugin.getConfig(), "update-checker");
            boolean enabled = ConfigSupport.getBoolean(section, "enabled", false);
            String repository = ConfigSupport.getString(section, "repository", "addxiaoyi/EnchADD").trim();
            if (!REPOSITORY_PATTERN.matcher(repository).matches()) {
                enabled = false;
                plugin.getLogger().warning("[EnchADD Update] Invalid GitHub repository setting; update checks are disabled.");
                repository = "addxiaoyi/EnchADD";
            }
            int timeout = Math.clamp(ConfigSupport.getInt(section, "timeout-seconds", 8), MIN_TIMEOUT_SECONDS, MAX_TIMEOUT_SECONDS);
            int intervalHours = Math.clamp(ConfigSupport.getInt(section, "interval-hours", DEFAULT_INTERVAL_HOURS), 1, MAX_INTERVAL_HOURS);
            boolean autoDownload = ConfigSupport.getBoolean(section, "auto-download", false);
            return new Settings(enabled, URI.create("https://api.github.com/repos/" + repository + "/releases/latest"),
                    timeout, intervalHours, autoDownload);
        }
    }
}