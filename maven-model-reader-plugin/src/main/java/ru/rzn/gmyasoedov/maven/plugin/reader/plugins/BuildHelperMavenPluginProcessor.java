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
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import ru.rzn.gmyasoedov.maven.plugin.reader.util.PluginUtils;

import java.util.List;

public class BuildHelperMavenPluginProcessor implements PluginProcessor {

    @Override
    public String groupId() {
        return "org.codehaus.mojo";
    }

    @Override
    public String artifactId() {
        return "build-helper-maven-plugin";
    }

    @Override
    public void process(MavenProject project, Plugin plugin) {
        List<PluginExecution> executions = plugin.getExecutions();
        for (PluginExecution execution : executions) {
            List<String> goals = execution.getGoals();
            if (goals == null) continue;
            if (goals.contains("add-source")) {
                List<String> pathList = PluginUtils.getPathList(execution.getConfiguration(), "sources");
                for (String path : pathList) {
                    project.addCompileSourceRoot(path);
                }
            }

            if (goals.contains("add-test-source")) {
                List<String> pathList = PluginUtils.getPathList(execution.getConfiguration(), "sources");
                for (String path : pathList) {
                    project.addTestCompileSourceRoot(path);
                }
            }

            if (goals.contains("add-resource")) {
                List<String> pathList = PluginUtils.getPathList(execution.getConfiguration(), "resources");
                for (String path : pathList) {
                    String absolutePath = PluginUtils.getAbsolutePath(path, project);
                    if (absolutePath != null) {
                        Resource resource = new Resource();
                        resource.setDirectory(absolutePath);
                        project.addResource(resource);
                    }
                }
            }
            if (goals.contains("add-test-resource")) {
                List<String> pathList = PluginUtils.getPathList(execution.getConfiguration(), "resources");
                for (String path : pathList) {
                    String absolutePath = PluginUtils.getAbsolutePath(path, project);
                    if (absolutePath != null) {
                        Resource resource = new Resource();
                        resource.setDirectory(absolutePath);
                        project.addTestResource(resource);
                    }
                }
            }
        }
    }
}
