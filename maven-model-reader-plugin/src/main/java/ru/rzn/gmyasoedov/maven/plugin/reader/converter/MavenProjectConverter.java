package ru.rzn.gmyasoedov.maven.plugin.reader.converter;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Resource;
import ru.rzn.gmyasoedov.maven.plugin.reader.BuildContext;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenArtifact;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenPlugin;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenProject;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static ru.rzn.gmyasoedov.maven.plugin.reader.util.MavenContextUtils.*;

public class MavenProjectConverter {
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.US);
    private static final int MAX_PROJECT_RECURSION_DEPTH = 3;

    public static MavenProject convert(org.apache.maven.project.MavenProject mavenProject,
                                       BuildContext context) {
        List<MavenPlugin> plugins = getPlugins(mavenProject, context);

        List<MavenArtifact> artifacts = new ArrayList<>(mavenProject.getArtifacts().size());
        for (Artifact artifact : mavenProject.getArtifacts()) {
            artifacts.add(MavenArtifactConverter.convert(artifact));
        }
        List<String> modulesDir = convertModules(mavenProject.getBasedir(), mavenProject.getModules());
        if (context.readOnly) {
            addReferencedProjects(mavenProject, artifacts, 0);
        }

        MavenProject result = new MavenProject();
        result.setGroupId(mavenProject.getGroupId());
        result.setArtifactId(mavenProject.getArtifactId());
        result.setVersion(mavenProject.getVersion());
        result.setPackaging(mavenProject.getPackaging());
        result.setName(mavenProject.getName());
        result.setBasedir(mavenProject.getBasedir().getAbsolutePath());
        result.setFilePath(mavenProject.getFile().getAbsolutePath());
        result.setParentFilePath(
                mavenProject.getParentFile() != null ? mavenProject.getParentFile().getAbsolutePath() : null
        );
        result.setModulesDir(modulesDir);
        result.setPlugins(plugins);
        result.setSourceRoots(mavenProject.getCompileSourceRoots());
        result.setTestSourceRoots(mavenProject.getTestCompileSourceRoots());
        result.setResourceRoots(convertResource(mavenProject.getResources(), context));
        result.setTestResourceRoots(convertResource(mavenProject.getTestResources(), context));
        result.setBuildDirectory(mavenProject.getBuild().getDirectory());
        result.setOutputDirectory(mavenProject.getBuild().getOutputDirectory());
        result.setTestOutputDirectory(mavenProject.getBuild().getTestOutputDirectory());
        result.setResolvedArtifacts(artifacts);
        if (context.addDependenciesInfo) {
            result.setDependencyArtifacts(convertMavenArtifact(mavenProject.getDependencyArtifacts()));
        }
        result.setParentArtifact(mavenProject.getParent() != null
                ? MavenArtifactConverter.convert(mavenProject.getParent()) : null);
        result.setProperties(getProperties(mavenProject));
        if (context.addRemoteRepositoryInfo) {
            List<ArtifactRepository> repositories = mavenProject.getRemoteArtifactRepositories();
            result.setRemoteRepositories(RemoteRepositoryConverter.convert(repositories));
        }

        result.setExcludedPaths(getExcludedPath(mavenProject));
        result.setGeneratedPath(getGeneratedPath(mavenProject));
        result.setTestGeneratedPath(getGeneratedTestPath(mavenProject));
        result.setAnnotationProcessorPaths(getAnnotationProcessorPath(mavenProject));

        return result;
    }

    private static List<MavenPlugin> getPlugins(
            org.apache.maven.project.MavenProject mavenProject, BuildContext context
    ) {
        if (!context.allPluginsInfo) {
            return Collections.emptyList();
        }
        List<MavenPlugin> plugins = new ArrayList<>(mavenProject.getBuildPlugins().size());
        for (Plugin plugin : mavenProject.getBuildPlugins()) {
            MavenPlugin convertedPlugin = MavenPluginConverter.convert(plugin, mavenProject);
            if (convertedPlugin != null) {
                plugins.add(convertedPlugin);
            }
        }
        return plugins;
    }

    private static Map<Object, Object> getProperties(org.apache.maven.project.MavenProject mavenProject) {
        Properties projectProperties = mavenProject.getProperties();
        if (projectProperties == null) {
            return Collections.emptyMap();
        } else {
            HashMap<Object, Object> result = new HashMap<>(projectProperties.size());
            result.putAll(projectProperties);
            return result;
        }
    }

    private static List<String> convertModules(File basedir, List<String> modules) {
        if (modules == null || modules.isEmpty()) return Collections.emptyList();
        ArrayList<String> result = new ArrayList<>(modules.size());
        for (String module : modules) {
            result.add(getModuleFile(basedir, module).getAbsolutePath());
        }
        return result;
    }

    private static List<MavenResource> convertResource(List<Resource> resources, BuildContext context) {
        if (resources == null || resources.isEmpty()) return Collections.emptyList();
        ArrayList<MavenResource> result = new ArrayList<>(resources.size());
        for (Resource item : resources) {
            MavenResource resource = new MavenResource();
            resource.setDirectory(item.getDirectory());
            if (context.fullResourceInfo) {
                resource.setExcludes(item.getExcludes());
                resource.setIncludes(item.getIncludes());
                resource.setFiltering(item.getFiltering());
                resource.setMergeId(item.getMergeId());
                resource.setTargetPath(item.getTargetPath());
            }
            result.add(resource);
        }
        return result;
    }

    private static List<MavenArtifact> convertMavenArtifact(Set<Artifact> artifacts) {
        if (artifacts == null || artifacts.isEmpty()) return Collections.emptyList();
        ArrayList<MavenArtifact> result = new ArrayList<>(artifacts.size());
        for (Artifact item : artifacts) {
            result.add(MavenArtifactConverter.convert(item));
        }
        return result;
    }

    private static File getModuleFile(File parentProjectFile, String relativePath) {
        relativePath = relativePath.replace('\\', File.separatorChar).replace('/', File.separatorChar);
        File moduleFile = new File(parentProjectFile, relativePath);
        if (moduleFile.isFile()) {
            moduleFile = moduleFile.getParentFile();
        }
        // we don't canonicalize on unix to avoid interfering with symlinks
        if (OS_NAME.contains("windows")) {
            try {
                moduleFile = moduleFile.getCanonicalFile();
            } catch (IOException e) {
                moduleFile = moduleFile.getAbsoluteFile();
            }
        } else {
            moduleFile = new File(moduleFile.toURI().normalize());
        }
        return moduleFile;
    }

    private static List<String> getExcludedPath(org.apache.maven.project.MavenProject project) {
        Object contextValue = project.getContextValue(EXCLUDED_PATHS);
        if (contextValue instanceof List) {
            return (List<String>) contextValue;
        }
        return Collections.emptyList();
    }

    private static List<String> getAnnotationProcessorPath(org.apache.maven.project.MavenProject project) {
        Object contextValue = project.getContextValue(ANNOTATION_PROCESSOR_PATHS);
        if (contextValue instanceof List) {
            return (List<String>) contextValue;
        }
        return Collections.emptyList();
    }

    private static String getGeneratedPath(org.apache.maven.project.MavenProject project) {
        Object contextValue = project.getContextValue(GENERATED_PATH);
        if (contextValue instanceof String && !((String) contextValue).isEmpty()) {
            return (String) contextValue;
        }
        return Paths.get(project.getBuild().getDirectory(), "generated-sources", "annotations").toString();
    }

    private static String getGeneratedTestPath(org.apache.maven.project.MavenProject project) {
        Object contextValue = project.getContextValue(GENERATED_TEST_PATH);
        if (contextValue instanceof String && !((String) contextValue).isEmpty()) {
            return (String) contextValue;
        }
        return Paths.get(project.getBuild().getDirectory(), "generated-test-sources", "test-annotations").toString();
    }

    private static void addReferencedProjects(
            org.apache.maven.project.MavenProject mavenProject, List<MavenArtifact> artifacts, int depth
    ) {
        if (depth > MAX_PROJECT_RECURSION_DEPTH) return;
        Map<String, org.apache.maven.project.MavenProject> references = mavenProject.getProjectReferences();
        if (references != null) {
            for (org.apache.maven.project.MavenProject each : references.values()) {
                MavenArtifact mavenArtifact = MavenArtifactConverter.convert(each);
                artifacts.add(mavenArtifact);
                // todo potential memory leak
                // solution: not create duplicate MavenArtifactConverter and save to map projectId(artifactId) -> subprojects
                //addReferencedProjects(each, artifacts, depth + 1);
            }
        }
    }
}