package ru.rzn.gmyasoedov.maven.plugin.reader.model.tree;

import java.io.Serializable;
import java.util.List;

public class MavenProjectDependencyTree implements Serializable {

    public String groupId;
    public String artifactId;
    public String version;
    public List<DependencyTreeNode> dependencies;

}
