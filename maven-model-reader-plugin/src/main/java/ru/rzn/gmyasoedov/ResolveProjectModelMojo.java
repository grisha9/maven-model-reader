package ru.rzn.gmyasoedov;

import com.google.gson.Gson;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import ru.rzn.gmyasoedov.converter.ListResultConverter;
import ru.rzn.gmyasoedov.converter.MapResultConverter;
import ru.rzn.gmyasoedov.model.MavenListResult;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;

import static java.lang.String.format;
import static org.apache.maven.plugins.annotations.LifecyclePhase.NONE;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

@Mojo(name = "resolve", defaultPhase = NONE, aggregator = true, requiresDependencyResolution = TEST, threadSafe = true)
public class ResolveProjectModelMojo extends GAbstractMojo {
    private static final String GMAVEN_PLUGIN_ANNOTATION_PROCESSOR = "gmaven.plugin.annotation.paths.%s";

    @Parameter(property = "annotationProcessingPluginArtifactIds", defaultValue = "")
    private String annotationProcessingPluginArtifactIds;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    private boolean skipResolve = false;

    @Override
    public void execute() throws MojoExecutionException {
        BuildContext context = getExecuteContext();
        skipResolve = context.readOnly;
        resolveArtifactErrors = new ArrayList<>();
        Set<String> gPluginSet = getPluginForBodyProcessing();
        getLog().info("ResolveProjectMojo: " + gPluginSet);
        if (session.getAllProjects() == null) return;
        for (MavenProject mavenProject : session.getAllProjects()) {
            resolvePluginBody(mavenProject, gPluginSet);
        }
        for (ArtifactResolutionException error : resolveArtifactErrors) {
            getLog().debug("Resolution of annotationProcessorPath dependencies failed: "
                    + error.getLocalizedMessage(), error);
        }

        Object result = getResult(context);
        printResult(result);
        //mvnDebug -f /home/Grigoriy.Myasoedov/jb/single-pom/pom.xml ru.rzn.gmyasoedov:maven-model-reader:1.0-SNAPSHOT:resolve
    }

    public void resolvePluginBody(MavenProject project, Set<String> gPlugins) throws MojoExecutionException {
        Model mavenModel = project.getModel();
        if (gPlugins.isEmpty() || mavenModel == null) return;

        Build build = mavenModel.getBuild();
        if (build != null) {
            List<Plugin> plugins = build.getPlugins();
            if (plugins != null) {
                for (Plugin each : plugins) {
                    processPlugin(each, gPlugins, project);
                }
            }
        }
    }

    private void processPlugin(Plugin each, Set<String> gPlugins, MavenProject project)
            throws MojoExecutionException {
        String pluginKey = each.getGroupId() + ":" + each.getArtifactId();
        if (!gPlugins.contains(pluginKey)) return;
        Map<String, Object> pluginBody = convertPluginBody(project, each);
        if (!pluginBody.isEmpty()) {
            String key = "gPlugin:" + pluginKey;
            project.setContextValue(key, pluginBody);
        }
    }

    private Map<String, Object> convertPluginBody(MavenProject project, Plugin plugin)
            throws MojoExecutionException {
        String annotationProcessorPaths = getPluginAnnotationProcessorPaths(plugin);
        List<String> resolvedPaths = resolveAnnotationProcessor(project, plugin, annotationProcessorPaths);
        List<Map<String, Object>> executions = new ArrayList<>(plugin.getExecutions().size());
        for (PluginExecution each : plugin.getExecutions()) {
            executions.add(convertExecution(each));
        }
        Map<String, Object> result = new HashMap<>(5);
        result.put("executions", executions);
        result.put("configuration", convertConfiguration(plugin.getConfiguration()));
        result.put("annotationProcessorPath", resolvedPaths);
        return result;
    }

    private static Map<String, Object> convertExecution(PluginExecution execution) {
        Map<String, Object> result = new HashMap<>(5);
        result.put("id", execution.getId());
        result.put("phase", execution.getPhase());
        result.put("goals", execution.getGoals());
        result.put("configuration", convertConfiguration(execution.getConfiguration()));
        return result;
    }

    private static String convertConfiguration(Object config) {
        if (config instanceof Xpp3Dom) {
            return config.toString();
        } else {
            return null;
        }
    }

    private Set<String> getPluginForBodyProcessing() {
        if (processingPluginIds == null || processingPluginIds.isEmpty()) return Collections.emptySet();
        String[] gPluginsArray = processingPluginIds.split(";");
        HashSet<String> gPluginSet = new HashSet<>(gPluginsArray.length * 2);
        Collections.addAll(gPluginSet, gPluginsArray);
        return gPluginSet;
    }

    private String getPluginAnnotationProcessorPaths(Plugin plugin) {
        if (skipResolve) return null;
        String path = System.getProperty(format(GMAVEN_PLUGIN_ANNOTATION_PROCESSOR, plugin.getArtifactId()), "");
        return path.isEmpty() ? null : path;
    }

    private List<String> resolveAnnotationProcessor(
            MavenProject project, Plugin plugin, String annotationProcessorPaths
    ) throws MojoExecutionException {
        if (annotationProcessorPaths == null || plugin == null || plugin.getConfiguration() == null) return null;
        List<DependencyCoordinate> dependencies = getDependencyCoordinates(plugin, annotationProcessorPaths);
        getLog().debug("Dependencies for resolve " + dependencies);
        List<String> paths = resolveArtifacts(dependencies, project, session);
        if (paths != null && !paths.isEmpty()) {
            getLog().info("annotation processor paths " + paths);
        }
        return paths;
    }

    private List<DependencyCoordinate> getDependencyCoordinates(Plugin plugin, String annotationProcessorPaths)
            throws MojoExecutionException {
        List<DependencyCoordinate> dependencies = new ArrayList<>();
        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        for (Xpp3Dom dom : configuration.getChildren()) {
            if (annotationProcessorPaths.equalsIgnoreCase(dom.getName())) {
                getLog().debug("annotationProcessorPaths=" + dom);
                for (Xpp3Dom child : dom.getChildren()) {
                    DependencyCoordinate coordinate = getDependencyCoordinate(child);
                    if (coordinate != null) {
                        dependencies.add(coordinate);
                    }
                }
                return dependencies;
            }
        }
        return dependencies;
    }

    private DependencyCoordinate getDependencyCoordinate(Xpp3Dom dom) throws MojoExecutionException {
        DependencyCoordinate coordinate = new DependencyCoordinate();
        for (Xpp3Dom child : dom.getChildren()) {
            String name = child.getName().toLowerCase();
            String value = child.getValue();
            if (value == null) continue;
            Field field = dependencyCoordinateFieldMap.get(name);
            if (field == null) continue;
            try {
                field.set(coordinate, value);
            } catch (IllegalAccessException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
        }
        return coordinate.getArtifactId() == null || coordinate.getGroupId() == null || coordinate.getVersion() == null
                ? null : coordinate;
    }

    private void printResult(Object result) {
        Path buildFilePath = session.getCurrentProject().getFile().toPath();
        Path parentPath = buildFilePath.getParent();
        Path resultPath = parentPath.resolve(".gmaven." + buildFilePath.getFileName());
        try {
            new Gson().toJson(result, new FileWriter(resultPath.toFile()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getResult(BuildContext context) {
        return context.resultAsTree
                ? MapResultConverter.convert(session, context) : ListResultConverter.convert(session, context);
    }

}