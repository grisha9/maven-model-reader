package ru.rzn.gmyasoedov.maven.plugin.reader.converter;

import org.apache.maven.project.MavenProject;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.tree.DependencyTreeNode;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.tree.MavenProjectDependencyTree;

import java.util.List;

public class MavenProjectDependencyTreeConverter {

    public static MavenProjectDependencyTree convert(
            MavenProject project,
            List<DependencyTreeNode> dependencyTreeNodes) {
        MavenProjectDependencyTree result = new MavenProjectDependencyTree();
        result.groupId = project.getGroupId();
        result.artifactId = project.getArtifactId();
        result.version = project.getVersion();
        result.dependencies = dependencyTreeNodes;
        return result;
    }
}
