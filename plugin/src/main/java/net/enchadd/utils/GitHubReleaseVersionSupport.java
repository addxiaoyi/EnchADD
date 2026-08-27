package net.enchadd.utils;

import org.jetbrains.annotations.NotNull;

final class GitHubReleaseVersionSupport {

    private GitHubReleaseVersionSupport() {
    }

    static boolean isNewer(@NotNull String candidate, @NotNull String current) {
        int[] candidateParts = parts(candidate);
        int[] currentParts = parts(current);
        if (candidateParts == null || currentParts == null) {
            return false;
        }
        for (int index = 0; index < candidateParts.length; index++) {
            int comparison = Integer.compare(candidateParts[index], currentParts[index]);
            if (comparison != 0) {
                return comparison > 0;
            }
        }
        return false;
    }

    private static int[] parts(String value) {
        String normalized = value.trim().replaceFirst("^[vV]", "");
        if (!normalized.matches("\\d+(?:\\.\\d+){0,2}")) {
            return null;
        }
        String[] rawParts = normalized.split("\\.");
        int[] parts = new int[3];
        try {
            for (int index = 0; index < rawParts.length; index++) {
                parts[index] = Integer.parseInt(rawParts[index]);
            }
            return parts;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
