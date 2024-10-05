package ru.rzn.gmyasoedov.maven.plugin.reader;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenMapResult;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public abstract class GAbstractMojo extends AbstractMojo {
    public static final String GMAVEN_POM_JSON = ".gmaven.pom.json";
    public static final String GMAVEN_TREE_JSON = ".gmaven.tree.json";
    @Component
    private RepositorySystem repositorySystem;
    @Component
    private ArtifactHandlerManager artifactHandlerManager;
    @Component
    private ResolutionErrorHandler resolutionErrorHandler;

    @Parameter(property = "processingPluginGAIds", defaultValue = "")
    protected Set<String> processingPluginGAIds;
    @Parameter(property = "resultFilePath", defaultValue = "")
    protected String resultFilePath;
    @Parameter(property = "addDependenciesInfo", defaultValue = "false")
    protected boolean addDependenciesInfo;
    @Parameter(property = "allPluginsInfo", defaultValue = "true")
    protected boolean allPluginsInfo;
    @Parameter(property = "addRemoteRepositoryInfo", defaultValue = "false")
    protected boolean addRemoteRepositoryInfo;
    @Parameter(property = "fullResourceInfo", defaultValue = "false")
    protected boolean fullResourceInfo;
    @Parameter(property = "resultAsTree", defaultValue = "false")
    protected boolean resultAsTree;
    @Parameter(property = "jsonPrettyPrinting", defaultValue = "false")
    protected boolean jsonPrettyPrinting;
    @Parameter(property = "incremental", defaultValue = "false")
    protected boolean incremental;

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
        return new BuildContext(
                addDependenciesInfo, allPluginsInfo, addRemoteRepositoryInfo,
                fullResourceInfo, resultAsTree, getIncrementalState()
        );
    }

    private MavenMapResult getIncrementalState() {
        if (!incremental) return null;
        if (filePathIsEmpty()) return null;
        Path path = Paths.get(resultFilePath);
        if (!path.toFile().exists()) return null;
        return getPreviousResult(path);
    }

    protected List<String> resolveArtifacts(
            List<DependencyCoordinate> dependencyCoordinates, MavenProject project, MavenSession session
    ) throws MojoExecutionException {
        if (dependencyCoordinates == null || dependencyCoordinates.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            Set<String> elements = new LinkedHashSet<>();
            for (DependencyCoordinate coord : dependencyCoordinates) {
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
            throw new MojoExecutionException("Resolution dependencies failed: " + e.getLocalizedMessage(), e);
        }
    }

    protected void printResult(Object result, MavenSession session) {
        Path resultPath = getResultPath(session);
        printResult(result, resultPath);
    }

    private Path getResultPath(MavenSession session) {
        if (filePathIsEmpty()) {
            Path path = getBuildDirectory(session.getTopLevelProject()).resolve(GMAVEN_POM_JSON);
            getLog().info("result file path: " + path);
            return path;
        }
        Path resultPath = Paths.get(resultFilePath);
        if (resultPath.toFile().isDirectory()) {
            throw new RuntimeException("Parameter resultFilePath is directory! Must be a file.");
        }
        return resultPath;
    }

    private boolean filePathIsEmpty() {
        return resultFilePath == null || resultFilePath.isEmpty();
    }

    protected void printResult(Object result, Path resultPath) {
        Path buildDirectory = resultPath.getParent();
        try {
            if (!buildDirectory.toFile().exists()) {
                Files.createDirectory(buildDirectory);
            }

            try (Writer writer = new FileWriter(resultPath.toFile())) {
                Gson gson = getGson();
                gson.toJson(result, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected MavenMapResult getPreviousResult(Path resultPath) {
        try {
            try (Reader reader = new FileReader(resultPath.toFile())) {
                return new Gson().fromJson(reader, MavenMapResult.class);
            }
        } catch (Exception e) {
            getLog().warn("Incremental previous state not found!", e);
            return null;
        }
    }

    private Gson getGson() {
        if (jsonPrettyPrinting) {
            return new GsonBuilder().setPrettyPrinting().create();
        }
        return new Gson();
    }

    private static Path getBuildDirectory(MavenProject mavenProject) {
        String buildDirectory = mavenProject.getBuild().getDirectory();
        if (buildDirectory == null) {
            return mavenProject.getBasedir().toPath();
        }
        Path path = Paths.get(buildDirectory);
        if (path.getFileName().toString().endsWith("target")) {
            return path;
        }
        return mavenProject.getBasedir().toPath();
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
