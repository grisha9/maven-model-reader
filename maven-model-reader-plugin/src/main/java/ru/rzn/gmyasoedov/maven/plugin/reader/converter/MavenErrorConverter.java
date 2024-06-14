package ru.rzn.gmyasoedov.maven.plugin.reader.converter;

import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.util.ExceptionUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.transfer.ArtifactTransferException;
import ru.rzn.gmyasoedov.maven.plugin.reader.GAbstractMojo;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.BuildErrors;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenException;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenId;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.SimpleMavenId;

import java.util.ArrayList;
import java.util.List;

public class MavenErrorConverter {

    public static BuildErrors convert(MavenExecutionResult result) {
        List<Throwable> exceptions = result.getExceptions();
        if (exceptions == null || exceptions.isEmpty())
            return new BuildErrors();

        boolean pluginNotResolved = false;
        List<MavenException> mavenExceptions = new ArrayList<>(exceptions.size());
        for (Throwable each : exceptions) {
            Throwable rootCause = getRootError(each);
            if (each instanceof PluginResolutionException) {
                PluginResolutionException e = (PluginResolutionException) each;
                Plugin plugin = e.getPlugin();
                if (plugin == null) continue;
                String packageName = GAbstractMojo.class.getPackage().getName();
                if (packageName.equalsIgnoreCase(plugin.getGroupId())
                        && "maven-model-reader-plugin".equalsIgnoreCase(plugin.getArtifactId())) {
                    pluginNotResolved = true;
                } else {
                    mavenExceptions.add(getMavenException(e.getMessage(), toMavenId(plugin), null));
                }
            } else if (rootCause instanceof ArtifactTransferException) {
                Artifact artifact = ((ArtifactTransferException) rootCause).getArtifact();
                String message = rootCause.getMessage() != null ? rootCause.getMessage() : each.getMessage();
                mavenExceptions.add(getMavenException(message, toMavenId(artifact), null));
            } else if (each instanceof ArtifactTransferException) {
                Artifact artifact = ((ArtifactTransferException) each).getArtifact();
                mavenExceptions.add(getMavenException(each.getMessage(), toMavenId(artifact), null));
            } else if (each instanceof ProjectBuildingException) {
                List<ProjectBuildingResult> results = ((ProjectBuildingException) each).getResults();
                for (ProjectBuildingResult buildingResult : results) {
                    for (ModelProblem problem : buildingResult.getProblems()) {
                        mavenExceptions.add(toMavenException(problem));
                    }
                }
            } else {
                String rootMessage = rootCause != null ? rootCause.getMessage() : null;
                String message = rootMessage != null ? rootMessage : each.getMessage();
                mavenExceptions.add(getMavenException(message, null, null));
            }
        }
        BuildErrors errors = new BuildErrors();
        errors.setPluginNotResolved(pluginNotResolved);
        errors.setExceptions(mavenExceptions);
        return errors;
    }

    private static MavenException toMavenException(ModelProblem problem) {
        String message = problem.getMessage();
        String source = problem.getSource();
        int lineNumber = problem.getLineNumber();
        int columnNumber = problem.getColumnNumber();
        String messageWithCoordinate = String.format("%s:%s:%s", source, lineNumber, columnNumber);
        message = message.replace(source, messageWithCoordinate);
        return getMavenException(message, null, source);
    }

    private static MavenException getMavenException(String message, MavenId mavenId, String source) {
        MavenException exception = new MavenException();
        exception.setMessage(message);
        exception.setProjectFilePath(source);
        exception.setMavenId(mavenId);
        return exception;
    }

    private static SimpleMavenId toMavenId(Plugin plugin) {
        if (plugin == null) return null;
        SimpleMavenId mavenId = new SimpleMavenId();
        mavenId.setArtifactId(plugin.getArtifactId());
        mavenId.setGroupId(plugin.getGroupId());
        mavenId.setVersion(plugin.getVersion());
        return mavenId;
    }

    private static SimpleMavenId toMavenId(Artifact plugin) {
        if (plugin == null) return null;
        SimpleMavenId mavenId = new SimpleMavenId();
        mavenId.setArtifactId(plugin.getArtifactId());
        mavenId.setGroupId(plugin.getGroupId());
        mavenId.setVersion(plugin.getVersion());
        return mavenId;
    }

    private static Throwable getRootError(Throwable each) {
        try {
            return ExceptionUtils.getRootCause(each);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return each;
    }
}
