package ru.rzn.gmyasoedov.converter;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import ru.rzn.gmyasoedov.BuildContext;
import ru.rzn.gmyasoedov.model.BuildErrors;
import ru.rzn.gmyasoedov.model.MavenListResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ListResultConverter {

    public static MavenListResult convert(MavenSession session, BuildContext context) {
        BuildErrors buildErrors = MavenErrorConverter.convert(session.getResult());
        MavenListResult result = new MavenListResult();
        result.pluginNotResolved = buildErrors.pluginNotResolved;
        result.exceptions = buildErrors.exceptions;
        result.settings = MavenSettingsConverter.convert(session);
        result.mavenProjects = getProjects(session.getAllProjects(), context);
        return result;
    }

    private static List<ru.rzn.gmyasoedov.model.MavenProject> getProjects(
            List<MavenProject> projects, BuildContext context
    ) {
        if (projects == null || projects.isEmpty()) return Collections.emptyList();
        List<ru.rzn.gmyasoedov.model.MavenProject> result = new ArrayList<>(projects.size());
        for (MavenProject each : projects) {
            result.add(MavenProjectConverter.convert(each, context));
        }
        return result;
    }
}
