package ru.rzn.gmyasoedov.maven.plugin.reader.converter;

import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import ru.rzn.gmyasoedov.maven.plugin.reader.BuildContext;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenMapResult;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenProjectContainer;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class MapResultConverter {

    public static MavenMapResult convert(MavenSession session, BuildContext context) {
        MavenMapResult result = new MavenMapResult();
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
        MavenProjectContainer container = new MavenProjectContainer(
                MavenProjectConverter.convert(topLevelProject, session, context)
        );
        fillContainer(container, projectByDirectoryMap, context, session);

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
                                      BuildContext context,
                                      MavenSession session) {
        ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenProject project = rootContainer.getProject();
        for (String module : project.getModulesDir()) {
            if (module == null || module.isEmpty()) continue;

            File moduleFile = new File(module);
            MavenProject mavenProjectByModuleFile = projectByDirectoryMap.get(moduleFile);
            if (mavenProjectByModuleFile == null) continue;

            MavenProjectContainer projectContainer = new MavenProjectContainer(
                    MavenProjectConverter.convert(mavenProjectByModuleFile, session, context)
            );
            rootContainer.getModules().add(projectContainer);
            fillContainer(projectContainer, projectByDirectoryMap, context, session);
        }
    }
}
