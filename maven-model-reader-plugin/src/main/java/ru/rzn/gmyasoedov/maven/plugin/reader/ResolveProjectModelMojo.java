package ru.rzn.gmyasoedov.maven.plugin.reader;

import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import ru.rzn.gmyasoedov.maven.plugin.reader.converter.ListResultConverter;
import ru.rzn.gmyasoedov.maven.plugin.reader.converter.MapResultConverter;
import ru.rzn.gmyasoedov.maven.plugin.reader.plugins.ApacheMavenCompilerPluginProcessor;
import ru.rzn.gmyasoedov.maven.plugin.reader.plugins.PluginProcessorManager;
import ru.rzn.gmyasoedov.maven.plugin.reader.util.MavenContextUtils;

import java.lang.reflect.Field;
import java.util.*;

import static org.apache.maven.plugins.annotations.LifecyclePhase.NONE;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;
import static ru.rzn.gmyasoedov.maven.plugin.reader.util.MavenContextUtils.ANNOTATION_PROCESSOR_PATHS;
import static ru.rzn.gmyasoedov.maven.plugin.reader.util.PluginUtils.PLUGIN_CONTEXT_PREFIX;

@Mojo(
        name = "resolve",
        defaultPhase = NONE,
        aggregator = true,
        requiresDependencyResolution = TEST,
        threadSafe = true
)
public class ResolveProjectModelMojo extends GAbstractMojo {

    @Parameter(defaultValue = "${session}")
    private MavenSession session;
    @Parameter(property = "resolvedPluginGAIds", defaultValue = "")
    protected Set<String> resolvedPluginGAIds;


    @Override
    public void execute() throws MojoExecutionException {
        BuildContext context = getExecuteContext();
        if (!context.readOnly && !getResolvedPluginGAIds().isEmpty()) {
            getLog().info("resolvedArtifactIds " + resolvedPluginGAIds);
        }
        if (context.previousResult != null) {
            printLogIncrementalUpdate();
        }
        resolveArtifactErrors = new ArrayList<>();
        Set<String> gaPluginSet = getGAPluginForBodyProcessing();
        getLog().info("ResolveProjectMojo: " + gaPluginSet);
        if (session.getAllProjects() == null) return;
        for (MavenProject mavenProject : session.getAllProjects()) {
            resolvePluginBody(mavenProject, gaPluginSet, context);
        }
        for (ArtifactResolutionException error : resolveArtifactErrors) {
            getLog().debug("Resolution of annotationProcessorPath dependencies failed: "
                    + error.getLocalizedMessage(), error);
        }

        Object result = getResult(context);
        printResult(result, session);
    }

    private void printLogIncrementalUpdate() {
        List<MavenProject> projects = session.getProjects();
        if (projects == null || projects.isEmpty()) return;
        getLog().info("Incremental update projects start:");
        for (MavenProject project : projects) {
            getLog().info(project.getArtifactId());
        }
        getLog().info("");
    }

    public void resolvePluginBody(
            MavenProject project, Set<String> gaPlugins, BuildContext context
    ) throws MojoExecutionException {
        Model mavenModel = project.getModel();
        if (gaPlugins.isEmpty() || mavenModel == null) return;

        Build build = mavenModel.getBuild();
        if (build != null) {
            List<Plugin> plugins = build.getPlugins();
            if (plugins == null) return;
            for (Plugin each : plugins) {
                processPlugin(each, gaPlugins, project, context);
            }
        }
    }

    private void processPlugin(Plugin each, Set<String> gaPlugins, MavenProject project, BuildContext context)
            throws MojoExecutionException {
        String pluginKey = each.getGroupId() + ":" + each.getArtifactId();
        if (!gaPlugins.contains(pluginKey)) return;
        PluginProcessorManager.process(project, each);
        Map<String, Object> pluginBody = convertPluginBody(project, each, context);
        if (!pluginBody.isEmpty()) {
            String key = PLUGIN_CONTEXT_PREFIX + pluginKey;
            project.setContextValue(key, pluginBody);
        }

        resolvePluginDependencies(each, project, context);
    }

    private void resolvePluginDependencies(
            Plugin each, MavenProject project, BuildContext context
    ) throws MojoExecutionException {
        if (context.readOnly) return;

        String pluginKey = each.getGroupId() + ":" + each.getArtifactId();
        if (getResolvedPluginGAIds().contains(pluginKey)) {
            resolve(each.getArtifactId(), each.getGroupId(), each.getVersion(), project);
            List<Dependency> dependencies = each.getDependencies();
            if (dependencies == null) return;
            for (Dependency dependency : dependencies) {
                resolve(dependency.getArtifactId(), dependency.getGroupId(), dependency.getVersion(), project);
            }
        }
    }

    private Map<String, Object> convertPluginBody(MavenProject project, Plugin plugin, BuildContext context)
            throws MojoExecutionException {
        List<String> resolvedPaths = resolveMavenCompilerAnnotationProcessor(project, plugin, context);
        if (!resolvedPaths.isEmpty()) {
            MavenContextUtils.addListStringValues(project, ANNOTATION_PROCESSOR_PATHS, resolvedPaths);
        }
        List<Map<String, Object>> executions = new ArrayList<>(plugin.getExecutions().size());
        for (PluginExecution each : plugin.getExecutions()) {
            executions.add(convertExecution(each));
        }
        Map<String, Object> result = new HashMap<>(5);
        result.put("executions", executions);
        result.put("configuration", convertConfiguration(plugin.getConfiguration()));
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

    private Set<String> getGAPluginForBodyProcessing() {
        return processingPluginGAIds == null ? Collections.<String>emptySet() : processingPluginGAIds;
    }

    private List<String> resolveMavenCompilerAnnotationProcessor(
            MavenProject project, Plugin plugin,
            BuildContext context) throws MojoExecutionException {
        if (context.readOnly || plugin == null || plugin.getConfiguration() == null
                || !ApacheMavenCompilerPluginProcessor.GROUP_ID.equals(plugin.getGroupId())
                || !ApacheMavenCompilerPluginProcessor.ARTIFACT_ID.equals(plugin.getArtifactId())) {
            return Collections.emptyList();
        }

        List<DependencyCoordinate> dependencies = getDependencyCoordinatesForAnnotationProccessor(plugin);
        getLog().debug("Dependencies for resolve " + dependencies);
        List<String> paths = resolveArtifacts(dependencies, project, session);
        if (!paths.isEmpty()) {
            getLog().info("annotation processor paths " + paths);
        }
        return paths;
    }

    private List<DependencyCoordinate> getDependencyCoordinatesForAnnotationProccessor(Plugin plugin)
            throws MojoExecutionException {
        List<DependencyCoordinate> dependencies = new ArrayList<>();
        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        for (Xpp3Dom dom : configuration.getChildren()) {
            if ("annotationProcessorPaths".equalsIgnoreCase(dom.getName())) {
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

    private Object getResult(BuildContext context) {
        return context.resultAsTree
                ? MapResultConverter.convert(session, context) : ListResultConverter.convert(session, context);
    }

    protected Set<String> getResolvedPluginGAIds() {
        return resolvedPluginGAIds != null ? resolvedPluginGAIds : Collections.<String>emptySet();
    }

    private void resolve(String artifactId, String groupId, String version, MavenProject project)
            throws MojoExecutionException {
        DependencyCoordinate coordinateDep = new DependencyCoordinate();
        coordinateDep.setArtifactId(artifactId);
        coordinateDep.setGroupId(groupId);
        coordinateDep.setVersion(version);
        getLog().info("gmaven.resolvedArtifactId " + coordinateDep);
        resolveArtifacts(Collections.singletonList(coordinateDep), project, session);
    }

}