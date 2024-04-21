package ru.rzn.gmyasoedov.converter;

import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import ru.rzn.gmyasoedov.BuildContext;
import ru.rzn.gmyasoedov.model.BuildErrors;
import ru.rzn.gmyasoedov.model.MavenListResult;
import ru.rzn.gmyasoedov.model.MavenMapResult;
import ru.rzn.gmyasoedov.model.MavenProjectContainer;

import java.io.File;
import java.util.*;


public class MapResultConverter {

    public static MavenMapResult convert(MavenSession session, BuildContext context) {
        BuildErrors buildErrors = MavenErrorConverter.convert(session.getResult());
        MavenMapResult result = new MavenMapResult();
        result.pluginNotResolved = buildErrors.pluginNotResolved;
        result.exceptions = buildErrors.exceptions;
        result.settings = MavenSettingsConverter.convert(session);
        result.container = getProjectsContainer(session, context);
        return result;
    }

    private static MavenProjectContainer getProjectsContainer(MavenSession session, BuildContext context) {
        List<MavenProject> projects = session.getAllProjects();
        if (projects == null || projects.isEmpty()) return null;
        MavenExecutionResult executionResult = session.getResult();
        if (executionResult == null || executionResult.getProject() == null) return null;
        MavenProject topLevelProject = executionResult.getProject();

        Map<File, MavenProject> projectByDirectoryMap = getMapForProjects(projects);
        for (MavenProject sortedProject : projects) {
            projectByDirectoryMap.put(sortedProject.getBasedir(), sortedProject);
        }
        MavenProjectContainer container = new MavenProjectContainer(MavenProjectConverter
                .convert(topLevelProject, context));
        fillContainer(container, projectByDirectoryMap, context);

        return container;
    }

    private static Map<File, MavenProject> getMapForProjects(List<MavenProject> projects) {
        if (projects.size() > 128) {
            return new TreeMap<>();
        } else {
            return new HashMap<>((int) (projects.size() * 1.5));
        }
    }

    private static void fillContainer(MavenProjectContainer rootContainer,
                                      Map<File, MavenProject> projectByDirectoryMap,
                                      BuildContext context) {
        ru.rzn.gmyasoedov.model.MavenProject project = rootContainer.getProject();
        for (String module : project.getModulesDir()) {
            if (StringUtils.isEmpty(module)) continue;

            File moduleFile = new File(module);
            MavenProject mavenProjectByModuleFile = projectByDirectoryMap.get(moduleFile);
            if (mavenProjectByModuleFile == null) continue;

            MavenProjectContainer projectContainer = new MavenProjectContainer(
                    MavenProjectConverter.convert(mavenProjectByModuleFile, context)
            );
            rootContainer.getModules().add(projectContainer);
            fillContainer(projectContainer, projectByDirectoryMap, context);
        }
    }
}
