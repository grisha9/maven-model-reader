package ru.rzn.gmyasoedov.model;

public interface MavenId {
    String UNKNOWN_VALUE = "Unknown";

    String getGroupId();

    String getArtifactId();

    String getVersion();
}
