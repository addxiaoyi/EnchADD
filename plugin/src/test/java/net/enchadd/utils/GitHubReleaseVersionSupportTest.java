package net.enchadd.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubReleaseVersionSupportTest {

    @Test
    void recognizesNewStableVersions() {
        assertTrue(GitHubReleaseVersionSupport.isNewer("v2.1.0", "2.0.5"));
        assertTrue(GitHubReleaseVersionSupport.isNewer("2.0.6", "2.0.5"));
        assertFalse(GitHubReleaseVersionSupport.isNewer("2.0.5", "2.0.5"));
        assertFalse(GitHubReleaseVersionSupport.isNewer("2.0.4", "2.0.5"));
    }

    @Test
    void rejectsNonReleaseVersionStrings() {
        assertFalse(GitHubReleaseVersionSupport.isNewer("main", "2.0.5"));
        assertFalse(GitHubReleaseVersionSupport.isNewer("2.1.0-beta", "2.0.5"));
    }
}
