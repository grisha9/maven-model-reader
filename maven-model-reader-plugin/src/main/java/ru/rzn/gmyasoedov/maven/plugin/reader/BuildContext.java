package ru.rzn.gmyasoedov.maven.plugin.reader;

import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenArtifact;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenMapResult;

import java.util.TreeMap;

public class BuildContext {
    public final boolean addDependenciesInfo;
    public final boolean allPluginsInfo;
    public final boolean addRemoteRepositoryInfo;
    public final boolean fullResourceInfo;
    public final boolean resultAsTree;
    public final boolean readOnly;
    public final MavenMapResult previousResult;
    public final TreeMap<String, MavenArtifact> readArtifactCache = new TreeMap<>();

    public BuildContext(
            boolean addDependenciesInfo,
            boolean allPluginsInfo,
            boolean addRemoteRepositoryInfo,
            boolean fullResourceInfo,
            boolean resultAsTree,
            MavenMapResult previousResult
    ) {
        this.addDependenciesInfo = addDependenciesInfo;
        this.allPluginsInfo = allPluginsInfo;
        this.addRemoteRepositoryInfo = addRemoteRepositoryInfo;
        this.fullResourceInfo = fullResourceInfo;
        this.resultAsTree = resultAsTree;
        this.readOnly = false;
        this.previousResult = previousResult;
    }

    public BuildContext(
            BuildContext context,
            boolean readOnly
    ) {
        this.addDependenciesInfo = context.addDependenciesInfo;
        this.allPluginsInfo = context.allPluginsInfo;
        this.addRemoteRepositoryInfo = context.addRemoteRepositoryInfo;
        this.fullResourceInfo = context.fullResourceInfo;
        this.resultAsTree = context.resultAsTree;
        this.readOnly = readOnly;
        this.previousResult = context.previousResult;
    }
}
