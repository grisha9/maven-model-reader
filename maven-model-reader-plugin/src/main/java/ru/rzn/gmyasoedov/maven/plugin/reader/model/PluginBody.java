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

package ru.rzn.gmyasoedov.maven.plugin.reader.model;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class PluginBody implements Serializable {
    private List<PluginExecution> executions = Collections.emptyList();
    private List<MavenArtifact> dependencies = Collections.emptyList();
    private String configuration;

    public List<PluginExecution> getExecutions() {
        return executions;
    }

    public void setExecutions(List<PluginExecution> executions) {
        this.executions = executions;
    }

    public List<MavenArtifact> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<MavenArtifact> dependencies) {
        this.dependencies = dependencies;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return "PluginBody{" +
                "executions=" + executions +
                ", dependencies=" + dependencies +
                ", configuration='" + configuration + '\'' +
                '}';
    }
}
