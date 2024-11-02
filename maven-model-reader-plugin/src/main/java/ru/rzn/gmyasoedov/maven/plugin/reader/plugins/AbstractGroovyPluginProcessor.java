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

package ru.rzn.gmyasoedov.maven.plugin.reader.plugins;


import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import ru.rzn.gmyasoedov.maven.plugin.reader.util.MavenContextUtils;
import ru.rzn.gmyasoedov.maven.plugin.reader.util.MavenJDOMUtil;
import ru.rzn.gmyasoedov.maven.plugin.reader.util.PluginUtils;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public abstract class AbstractGroovyPluginProcessor {

    public static void process(MavenProject project, Plugin plugin) {
        Object mainConfiguration = null;
        Object testConfiguration = null;
        Object stubConfiguration = null;
        Object testStubConfiguration = null;
        List<PluginExecution> executions = plugin.getExecutions();
        for (PluginExecution execution : executions) {
            List<String> goals = execution.getGoals();
            if (goals == null) continue;
            if (goals.contains("compile")) {
                mainConfiguration = execution.getConfiguration();
            }

            if (goals.contains("testCompile") || goals.contains("compileTests")) {
                testConfiguration = execution.getConfiguration();
            }

            if (goals.contains("generateStubs")) {
                stubConfiguration = execution.getConfiguration();
            }
            if (goals.contains("generateTestStubs")) {
                testStubConfiguration = execution.getConfiguration();
            }
        }
        addSourcePaths(mainConfiguration, project, false);
        addSourcePaths(testConfiguration, project, true);
        MavenContextUtils.addListStringValue(project, MavenContextUtils.EXCLUDED_PATHS, getExcludedPath(project, stubConfiguration));
        MavenContextUtils.addListStringValue(project, MavenContextUtils.EXCLUDED_PATHS, getExcludedPath(project, testStubConfiguration));
    }

    private static String getExcludedPath(MavenProject mavenProject, Object config) {
        if (config == null) return getDefaultExcludedDir(mavenProject);
        String outputDirectory = PluginUtils.getValue(config, "outputDirectory");
        return outputDirectory != null ? PluginUtils.getAbsolutePath(outputDirectory, mavenProject)
                : getDefaultExcludedDir(mavenProject);
    }

    private static String getDefaultExcludedDir(MavenProject mavenProject) {
        return Paths.get(mavenProject.getBuild().getDirectory(), "generated-sources", "groovy-stubs").toString();
    }

    private static void addSourcePaths(Object config, MavenProject mavenProject, Boolean isTest) {
        Xpp3Dom currentTag = config instanceof Xpp3Dom ? ((Xpp3Dom) config) : null;

        List<String> paths;
        if (currentTag == null) {
            paths = getDefaultPath(mavenProject, isTest);
        } else {
            paths = MavenJDOMUtil.findChildrenValuesByPath(currentTag, "sources", "fileset.directory");
            if (paths.isEmpty()) {
                paths = MavenJDOMUtil.findChildrenValuesByPath(
                        currentTag,
                        isTest ? "testSources" : "sources",
                        isTest ? "testSource.directory" : "source.directory"
                );
            }
            if (paths.isEmpty()) {
                paths = getDefaultPath(mavenProject, isTest);
            }
        }

        for (String path : paths) {
            if (isTest) {
                mavenProject.addTestCompileSourceRoot(path);
            } else {
                mavenProject.addCompileSourceRoot(path);
            }
        }
    }

    private static List<String> getDefaultPath(MavenProject mavenProject, Boolean isTest) {
        String sourceFolderName = isTest ? "test" : "main";
        return Collections.singletonList(
                Paths.get(mavenProject.getBasedir().getAbsolutePath(), "src", sourceFolderName, "groovy").toString()
        );
    }
}
