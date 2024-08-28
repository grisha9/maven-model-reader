package ru.rzn.gmyasoedov.maven.plugin.reader.converter;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenArtifact;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenArtifactNode;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenArtifactState;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.tree.DependencyTreeNode;

import java.util.*;

public class DependencyTreeNodeConverter {

    public static List<DependencyTreeNode> convert(
            Collection<? extends DependencyNode> nodes,
            Map<Artifact, MavenArtifact> convertedArtifactMap) {
        if (nodes == null || nodes.isEmpty()) return Collections.emptyList();

        List<DependencyTreeNode> result = new ArrayList<>(nodes.size());
        for (DependencyNode each : nodes) {
            Artifact artifactNode = toArtifact(each.getDependency());
            MavenArtifactState state = MavenArtifactState.ADDED;
            Object winner = each.getData().get(ConflictResolver.NODE_DATA_WINNER);

            MavenArtifactNode winnerArtifact = null;
            MavenArtifact mavenArtifact;

            if (winner instanceof DependencyNode) {
                DependencyNode winnerNode = (DependencyNode) winner;
                winnerArtifact = convertToNode(toArtifact(winnerNode.getDependency()));
                mavenArtifact = MavenArtifactConverter.convert(artifactNode);
                if (!Objects.equals(each.getVersion().toString(), winnerNode.getVersion().toString())) {
                    state = MavenArtifactState.CONFLICT;
                } else {
                    state = MavenArtifactState.DUPLICATE;
                }
            } else {
                mavenArtifact = getArtifact(artifactNode, convertedArtifactMap);
            }

            //String premanagedVersion = DependencyManagerUtils.getPremanagedVersion(each);
            //String premanagedScope = DependencyManagerUtils.getPremanagedScope(each);
            DependencyTreeNode newNode = new DependencyTreeNode();
            newNode.artifact = convertToNode(mavenArtifact);
            newNode.relatedArtifact = winnerArtifact;
            newNode.state = state;
            newNode.originalScope = each.getDependency().getScope();
            newNode.dependencies = convert(each.getChildren(), convertedArtifactMap);
            result.add(newNode);
        }
        return result;
    }

    private static Artifact toArtifact(Dependency dependency) {
        if (dependency == null) {
            return null;
        }

        Artifact result = RepositoryUtils.toArtifact(dependency.getArtifact());
        if (result == null) {
            return null;
        }
        result.setScope(dependency.getScope());
        result.setOptional(dependency.isOptional());
        return result;
    }

    public static MavenArtifact getArtifact(Artifact artifact, Map<Artifact, MavenArtifact> convertedArtifactMap) {
        MavenArtifact result = convertedArtifactMap.get(artifact);
        if (result == null) {
            result = MavenArtifactConverter.convert(artifact);
            convertedArtifactMap.put(artifact, result);
        }
        return result;
    }

    public static MavenArtifactNode convertToNode(Artifact artifact) {
        MavenArtifactNode result = new MavenArtifactNode();
        result.setGroupId(artifact.getGroupId());
        result.setArtifactId(artifact.getArtifactId());
        result.setVersion(artifact.getBaseVersion() != null ? artifact.getBaseVersion() : artifact.getVersion());
        result.setType(artifact.getType());
        result.setClassifier(artifact.getClassifier());
        result.setScope(artifact.getScope());
        result.setOptional(artifact.isOptional());
        result.setFilePath(artifact.getFile() == null ? null : artifact.getFile().getAbsolutePath());
        result.setResolved(artifact.isResolved());
        return result;
    }

    private static MavenArtifactNode convertToNode(MavenArtifact artifact) {
        MavenArtifactNode result = new MavenArtifactNode();
        result.setGroupId(artifact.getGroupId());
        result.setArtifactId(artifact.getArtifactId());
        result.setVersion(artifact.getVersion());
        result.setType(artifact.getType());
        result.setClassifier(artifact.getClassifier());
        result.setScope(artifact.getScope());
        result.setOptional(artifact.isOptional());
        result.setFilePath(artifact.getFilePath());
        result.setResolved(artifact.isResolved());
        return result;
    }
}
