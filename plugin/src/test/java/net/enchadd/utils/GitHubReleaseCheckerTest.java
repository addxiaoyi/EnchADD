package net.enchadd.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubReleaseCheckerTest {

    @Test
    void readsStableGitHubReleasePayloadAndFindsProtectedAsset() {
        String payload = "{\"tag_name\":\"v2.1.1\",\"html_url\":\"https://github.com/addxiaoyi/EnchADD/releases/tag/v2.1.1\",\"prerelease\":false,\"assets\":[{\"name\":\"enchadd-plugin-2.1.1-protected.jar\",\"browser_download_url\":\"https://github.com/addxiaoyi/EnchADD/releases/download/v2.1.1/enchadd-plugin-2.1.1-protected.jar\",\"digest\":\"sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef\"}]}";

        GitHubReleaseChecker.Release release = GitHubReleaseChecker.parseRelease(payload).orElseThrow();

        assertEquals("v2.1.1", release.version());
        assertTrue(release.url().startsWith("https://github.com/addxiaoyi/"));
        assertEquals("enchadd-plugin-2.1.1-protected.jar", release.pluginAsset().name());
        assertTrue(release.pluginAsset().digest().startsWith("sha256:"));
    }

    @Test
    void ignoresPrereleasesAndUntrustedDownloadPages() {
        assertTrue(GitHubReleaseChecker.parseRelease("{\"tag_name\":\"v2.1.1\",\"html_url\":\"https://github.com/addxiaoyi/EnchADD/releases/tag/v2.1.1\",\"prerelease\":true}").isEmpty());
        assertTrue(GitHubReleaseChecker.parseRelease("{\"tag_name\":\"v2.1.1\",\"html_url\":\"https://invalid.example/update\",\"prerelease\":false}").isEmpty());
    }
}