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

package ru.rzn.gmyasoedov.maven.plugin.reader.util;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PluginUtils {
    public static final String PLUGIN_CONTEXT_PREFIX = "gPlugin:";

    public static List<String> getPathList(Object config, String parameterName) {
        Xpp3Dom sourceDirs = config instanceof Xpp3Dom ? ((Xpp3Dom) config).getChild(parameterName) : null;
        if (sourceDirs == null) return Collections.emptyList();
        Xpp3Dom[] pathElements = sourceDirs.getChildren();
        ArrayList<String> result = new ArrayList<>(1);
        for (Xpp3Dom sourceDirElement : pathElements) {
            String value = sourceDirElement.getValue();
            if (value != null) result.add(value);
        }
        return result;
    }

    public static String getValue(Object config, String... parameters) {
        Xpp3Dom currentTag = config instanceof Xpp3Dom ? ((Xpp3Dom) config) : null;
        if (currentTag == null) return null;

        for (String parameter : parameters) {
            currentTag = currentTag.getChild(parameter);
            if (currentTag == null) return null;
        }
        return currentTag.getValue();
    }

    public static Xpp3Dom getConfiguration(Plugin plugin, String goal) {
        Object configuration = plugin.getConfiguration();

        if (configuration == null && goal != null) {
            configuration = findConfiguration(plugin.getExecutions(), goal);

        }
        return configuration instanceof Xpp3Dom ? (Xpp3Dom) configuration : null;
    }

    private static Xpp3Dom findConfiguration(List<PluginExecution> executions, String goal) {
        if (executions == null) return null;
        for (PluginExecution execution : executions) {
            if (execution.getGoals().contains(goal)) {
                return execution.getConfiguration() instanceof Xpp3Dom ? (Xpp3Dom) execution.getConfiguration() : null;
            }
        }
        return null;
    }

    public static String getAbsolutePath(String mavenPath, MavenProject mavenProject) {
        if (mavenPath == null) return null;
        try {
            Path path = Paths.get(mavenPath);
            if (path.isAbsolute()) {
                return mavenPath;
            } else {
                return Paths.get(mavenProject.getBasedir().getAbsolutePath(), mavenPath).toAbsolutePath().toString();
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static String getAbsolutePath(String mavenPath, String baseAbsolutePath) {
        if (mavenPath == null) return null;
        try {
            Path path = Paths.get(mavenPath);
            if (path.isAbsolute()) {
                return mavenPath;
            } else {
                return Paths.get(baseAbsolutePath, mavenPath).toAbsolutePath().toString();
            }
        } catch (Exception e) {
            return null;
        }
    }

}
