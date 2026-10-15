package net.enchadd.mod.common;

public record ModVersion(int major, int minor, int patch) implements Comparable<ModVersion> {

    public static final ModVersion MINIMUM = new ModVersion(1, 21, 11);
    public static final ModVersion MAXIMUM = new ModVersion(26, 3, 0);

    public boolean isSupported() {
        return compareTo(MINIMUM) >= 0 && compareTo(MAXIMUM) <= 0;
    }

    public boolean isModern26() {
        return major >= 26;
    }

    @Override
    public int compareTo(ModVersion other) {
        int majorCompare = Integer.compare(major, other.major);
        if (majorCompare != 0) return majorCompare;
        int minorCompare = Integer.compare(minor, other.minor);
        return minorCompare != 0 ? minorCompare : Integer.compare(patch, other.patch);
    }
}
