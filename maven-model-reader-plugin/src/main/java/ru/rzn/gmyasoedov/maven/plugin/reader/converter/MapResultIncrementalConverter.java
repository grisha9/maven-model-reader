/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package ru.rzn.gmyasoedov.maven.plugin.reader.converter;

import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import ru.rzn.gmyasoedov.maven.plugin.reader.BuildContext;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenMapResult;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenProject;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenProjectContainer;

import java.util.*;


public class MapResultIncrementalConverter {

    public static MavenMapResult convert(MavenSession session, BuildContext context) {
        MavenMapResult result = Objects.requireNonNull(context.previousResult);
        result.settings = MavenSettingsConverter.convert(session);
        result.container = updateProjectContainer(session, result.container, context);
        return result;
    }

    private static MavenProjectContainer updateProjectContainer(
            MavenSession session, MavenProjectContainer previousResult, BuildContext context
    ) {
        List<org.apache.maven.project.MavenProject> projects = session.getProjects();
        if (projects == null || projects.isEmpty()) return null;
        MavenExecutionResult executionResult = session.getResult();
        if (executionResult == null || executionResult.getProject() == null) return null;

        Map<String, MavenProject> projectByArtifactIdMap = getProjectByArtifactIdMap(projects);
        for (org.apache.maven.project.MavenProject each : projects) {
            projectByArtifactIdMap.put(each.getArtifactId(), MavenProjectConverter.convert(each, session, context));
        }

        fillContainer(previousResult, projectByArtifactIdMap);
        return previousResult;
    }

    private static Map<String, MavenProject> getProjectByArtifactIdMap(
            List<org.apache.maven.project.MavenProject> projects
    ) {
        if (projects.size() > 128) {
            return new TreeMap<>();
        } else {
            return new HashMap<>((int) (projects.size() * 1.5));
        }
    }

    private static void fillContainer(
            MavenProjectContainer rootContainer, Map<String, MavenProject> projectByArtifactIdMap
    ) {
        MavenProject oldProject = rootContainer.getProject();
        MavenProject updatedMavenProject = projectByArtifactIdMap.get(oldProject.getArtifactId());
        if (updatedMavenProject != null) {
            rootContainer.setProject(updatedMavenProject);
        }
        for (MavenProjectContainer container : rootContainer.getModules()) {
            fillContainer(container, projectByArtifactIdMap);
        }
    }
}
