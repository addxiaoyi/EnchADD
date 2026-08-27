package net.enchadd.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubReleaseCheckerTest {

    @Test
    void readsStableGitHubReleasePayload() {
        String payload = "{\"tag_name\":\"v2.0.6\",\"html_url\":\"https://github.com/EnchADD/EnchADD/releases/tag/v2.0.6\",\"prerelease\":false}";

        GitHubReleaseChecker.Release release = GitHubReleaseChecker.parseRelease(payload).orElseThrow();

        assertEquals("v2.0.6", release.version());
        assertTrue(release.url().startsWith("https://github.com/EnchADD/"));
    }

    @Test
    void ignoresPrereleasesAndUntrustedDownloadPages() {
        assertTrue(GitHubReleaseChecker.parseRelease("{\"tag_name\":\"v2.1.0\",\"html_url\":\"https://github.com/EnchADD/EnchADD/releases/tag/v2.1.0\",\"prerelease\":true}").isEmpty());
        assertTrue(GitHubReleaseChecker.parseRelease("{\"tag_name\":\"v2.1.0\",\"html_url\":\"https://invalid.example/update\",\"prerelease\":false}").isEmpty());
    }
}
