package ru.rzn.gmyasoedov.maven.plugin.reader.model.tree;

import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenArtifactNode;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenArtifactState;

import java.io.Serializable;
import java.util.List;

public class DependencyTreeNode implements Serializable {

    public MavenArtifactNode artifact;
    public MavenArtifactNode relatedArtifact;
    public MavenArtifactState state;
    public String originalScope;
    public List<DependencyTreeNode> dependencies;

}
