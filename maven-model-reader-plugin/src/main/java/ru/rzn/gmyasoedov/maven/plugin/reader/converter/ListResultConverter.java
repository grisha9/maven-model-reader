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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import ru.rzn.gmyasoedov.maven.plugin.reader.BuildContext;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenListResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ListResultConverter {

    public static MavenListResult convert(MavenSession session, BuildContext context) {
        MavenListResult result = new MavenListResult();
        result.settings = MavenSettingsConverter.convert(session);
        result.mavenProjects = getProjects(session, context);
        return result;
    }

    private static List<ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenProject> getProjects(
            MavenSession session, BuildContext context
    ) {
        List<MavenProject> projects = session.getAllProjects();
        if (projects == null || projects.isEmpty()) return Collections.emptyList();
        List<ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenProject> result = new ArrayList<>(projects.size());
        for (MavenProject each : projects) {
            result.add(MavenProjectConverter.convert(each, session, context));
        }
        return result;
    }
}
