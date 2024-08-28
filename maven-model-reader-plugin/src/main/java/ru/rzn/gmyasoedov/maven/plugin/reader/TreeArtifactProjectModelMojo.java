package ru.rzn.gmyasoedov.maven.plugin.reader;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.graph.DependencyNode;
import ru.rzn.gmyasoedov.maven.plugin.reader.converter.DependencyTreeNodeConverter;
import ru.rzn.gmyasoedov.maven.plugin.reader.converter.MavenProjectDependencyTreeConverter;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenArtifact;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.tree.DependencyTreeNode;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.tree.MavenProjectDependencyTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.apache.maven.plugins.annotations.LifecyclePhase.NONE;

@Mojo(
        name = "tree",
        defaultPhase = NONE,
        aggregator = true,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true
)
public class TreeArtifactProjectModelMojo extends GAbstractMojo {

    @Parameter(defaultValue = "${session}")
    private MavenSession session;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<MavenProject> projects = session.getProjects();
        if (projects == null) return;

        ArrayList<MavenProjectDependencyTree> result = new ArrayList<>(projects.size());
        for (MavenProject project : projects) {
            DependencyNode dependencyNode = getDependencyNode(project);
            if (isEmpty(dependencyNode)) continue;
            List<DependencyTreeNode> treeNodes = DependencyTreeNodeConverter
                    .convert(dependencyNode.getChildren(), new HashMap<Artifact, MavenArtifact>());
            result.add(MavenProjectDependencyTreeConverter.convert(project, treeNodes));
        }
        if (result.isEmpty()) return;
        printResult(result, session, GMAVEN_TREE_JSON);
    }

    private static boolean isEmpty(DependencyNode dependencyNode) {
        return dependencyNode == null || dependencyNode.getChildren() == null || dependencyNode.getChildren().isEmpty();
    }

    private static DependencyNode getDependencyNode(MavenProject project) {
        Object contextValue = project.getContextValue("gmaven-dependency-graph");
        if (contextValue instanceof DependencyNode) {
            return (DependencyNode) contextValue;
        }
        return null;
    }
}