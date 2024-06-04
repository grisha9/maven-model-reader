package ru.rzn.gmyasoedov;


import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public abstract class GAbstractMojo extends AbstractMojo {
    @Component
    private RepositorySystem repositorySystem;
    @Component
    private ArtifactHandlerManager artifactHandlerManager;
    @Component
    private ResolutionErrorHandler resolutionErrorHandler;

    @Parameter(property = "processingPluginGAIds", defaultValue = "")
    protected String processingPluginGAIds;
    @Parameter(property = "addDependenciesInfo", defaultValue = "false")
    protected boolean addDependenciesInfo;
    @Parameter(property = "allPluginsInfo", defaultValue = "false")
    protected boolean allPluginsInfo;
    @Parameter(property = "addRemoteRepositoryInfo", defaultValue = "false")
    protected boolean addRemoteRepositoryInfo;
    @Parameter(property = "fullResourceInfo", defaultValue = "false")
    protected boolean fullResourceInfo;
    @Parameter(property = "resultAsTree", defaultValue = "false")
    protected boolean resultAsTree;

    protected final Map<String, Field> dependencyCoordinateFieldMap = getDependencyCoordinateFieldMap();
    protected List<ArtifactResolutionException> resolveArtifactErrors = new ArrayList<>();

    private Map<String, Field> getDependencyCoordinateFieldMap() {
        Field[] fields = DependencyCoordinate.class.getDeclaredFields();
        Map<String, Field> map = new HashMap<>();
        for (Field field : fields) {
            field.setAccessible(true);
            map.put(field.getName().toLowerCase(), field);
        }
        return map;
    }

    protected BuildContext getExecuteContext() {
        return new BuildContext(addDependenciesInfo, allPluginsInfo, addRemoteRepositoryInfo, fullResourceInfo, resultAsTree);
    }

    protected List<String> resolveArtifacts(
            List<DependencyCoordinate> annotationProcessorPaths,
            MavenProject project,
            MavenSession session
    ) throws MojoExecutionException {
        if (annotationProcessorPaths == null || annotationProcessorPaths.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            Set<String> elements = new LinkedHashSet<>();
            for (DependencyCoordinate coord : annotationProcessorPaths) {
                ArtifactHandler handler = artifactHandlerManager.getArtifactHandler(coord.getType());

                Artifact artifact = new DefaultArtifact(
                        coord.getGroupId(),
                        coord.getArtifactId(),
                        VersionRange.createFromVersionSpec(coord.getVersion()),
                        Artifact.SCOPE_RUNTIME,
                        coord.getType(),
                        coord.getClassifier(),
                        handler,
                        false);

                ArtifactResolutionRequest request = new ArtifactResolutionRequest()
                        .setArtifact(artifact)
                        .setResolveRoot(true)
                        .setResolveTransitively(true)
                        .setLocalRepository(session.getLocalRepository())
                        .setRemoteRepositories(project.getRemoteArtifactRepositories());

                ArtifactResolutionResult resolutionResult = repositorySystem.resolve(request);

                resolutionErrorHandler.throwErrors(request, resolutionResult);

                for (Artifact resolved : resolutionResult.getArtifacts()) {
                    elements.add(resolved.getFile().getAbsolutePath());
                }
            }
            return new ArrayList<>(elements);
        } catch (ArtifactResolutionException e) {
            File file = tryGetLocalArtifactFromTarget(project);
            if (file != null) {
                return Collections.singletonList(file.getAbsolutePath());
            }
            resolveArtifactErrors.add(e);
            return Collections.emptyList();
        } catch (Exception e) {
            throw new MojoExecutionException("Resolution of annotationProcessorPath dependencies failed: "
                    + e.getLocalizedMessage(), e);
        }
    }

    private static File tryGetLocalArtifactFromTarget(MavenProject project) {
        try {
            File[] files = new File(project.getBuild().getDirectory()).listFiles();
            if (files == null) return null;
            List<File> results = new ArrayList<>(2);
            for (File file : files) {
                if (!file.isDirectory() && file.getName().endsWith(".jar")) {
                    results.add(file);
                }
            }
            if (results.size() == 1) return results.get(0);
            for (File result : results) {
                if (result.getName().contains(project.getArtifactId())) {
                    return result;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

}
