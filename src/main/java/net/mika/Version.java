package net.mika;

public class Version {
    public String stringVersion;
    public boolean isForge;
    public boolean isFabric;

    public Version(String version, boolean isForge, boolean isFabric) {
        this.stringVersion = version;
        this.isForge = isForge;
        this.isFabric = isFabric;
    }
}
