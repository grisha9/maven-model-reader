package ru.rzn.gmyasoedov.maven.plugin.reader.converter;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenArtifact;

import static ru.rzn.gmyasoedov.maven.plugin.reader.util.ObjectUtils.emptyStringIfNull;


public class MavenArtifactConverter {
    public static MavenArtifact convert(Artifact artifact) {
        MavenArtifact result = new MavenArtifact();
        result.setGroupId(emptyStringIfNull(artifact.getGroupId()));
        result.setArtifactId(emptyStringIfNull(artifact.getArtifactId()));
        String version = artifact.getBaseVersion() != null ? artifact.getBaseVersion() : artifact.getVersion();
        result.setVersion(emptyStringIfNull(version));
        result.setType(artifact.getType());
        result.setClassifier(artifact.getClassifier());
        result.setScope(artifact.getScope());
        result.setOptional(artifact.isOptional());
        result.setFilePath(artifact.getFile() != null ? artifact.getFile().getAbsolutePath() : null);
        result.setResolved(artifact.isResolved());
        return result;
    }

    public static MavenArtifact convert(MavenProject project) {
        MavenArtifact result = new MavenArtifact();
        result.setGroupId(project.getGroupId());
        result.setArtifactId(project.getArtifactId());
        result.setVersion(project.getVersion());
        result.setType(project.getPackaging());
        result.setFilePath(project.getFile() != null ? project.getFile().getAbsolutePath() : null);
        result.setResolved(true);
        return result;
    }
}
