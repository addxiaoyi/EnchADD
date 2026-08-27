package net.enchadd.utils;

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
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Pattern;

/** Queries the configured public GitHub release feed without downloading executable code. */
public final class GitHubReleaseChecker {

    private static final Pattern REPOSITORY_PATTERN = Pattern.compile("[A-Za-z0-9_.-]{1,100}/[A-Za-z0-9_.-]{1,100}");
    private static final int MIN_TIMEOUT_SECONDS = 2;
    private static final int MAX_TIMEOUT_SECONDS = 30;
    private static final int MAX_RESPONSE_BYTES = 64 * 1024;
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(MAX_TIMEOUT_SECONDS))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private BukkitTask task;

    public void start(@NotNull JavaPlugin plugin) {
        Settings settings = Settings.from(plugin);
        if (!settings.enabled()) {
            return;
        }
        stop();
        task = plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> check(plugin, settings));
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void check(JavaPlugin plugin, Settings settings) {
        try {
            Optional<Release> release = fetch(settings);
            if (release.isEmpty()) {
                return;
            }
            String current = plugin.getPluginMeta().getVersion();
            Release latest = release.get();
            if (GitHubReleaseVersionSupport.isNewer(latest.version(), current)) {
                plugin.getLogger().warning("[EnchADD Update] New version " + latest.version()
                        + " is available (current " + current + "): " + latest.url());
            } else {
                plugin.getLogger().info("[EnchADD Update] Current version " + current + " is up to date.");
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

    private static Optional<Release> fetch(Settings settings) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(settings.apiUri())
                .timeout(Duration.ofSeconds(settings.timeoutSeconds()))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "EnchADD-Update-Checker")
                .GET()
                .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404) {
            return Optional.empty();
        }
        if (response.statusCode() != 200) {
            throw new IOException("GitHub API returned HTTP " + response.statusCode());
        }
        String body = response.body();
        if (body.length() > MAX_RESPONSE_BYTES) {
            throw new IOException("GitHub API response exceeded " + MAX_RESPONSE_BYTES + " characters");
        }
        return parseRelease(body);
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
        return Optional.of(new Release(tag, url));
    }

    private static String stringValue(JsonObject object, String member) {
        JsonElement value = object.get(member);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                ? value.getAsString().trim()
                : "";
    }

    record Release(@NotNull String version, @NotNull String url) {
    }

    private record Settings(boolean enabled, @NotNull URI apiUri, int timeoutSeconds) {
        static Settings from(JavaPlugin plugin) {
            ConfigurationSection section = ConfigSupport.getConfigSection(plugin.getConfig(), "update-checker");
            boolean enabled = ConfigSupport.getBoolean(section, "enabled", false);
            String repository = ConfigSupport.getString(section, "repository", "EnchADD/EnchADD").trim();
            if (!REPOSITORY_PATTERN.matcher(repository).matches()) {
                enabled = false;
                plugin.getLogger().warning("[EnchADD Update] Invalid GitHub repository setting; update checks are disabled.");
                repository = "EnchADD/EnchADD";
            }
            int timeout = Math.clamp(
                    ConfigSupport.getInt(section, "timeout-seconds", 8),
                    MIN_TIMEOUT_SECONDS,
                    MAX_TIMEOUT_SECONDS
            );
            return new Settings(enabled, URI.create("https://api.github.com/repos/" + repository + "/releases/latest"), timeout);
        }
    }
}
