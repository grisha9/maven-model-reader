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

package ru.rzn.gmyasoedov.maven.plugin.reader;

import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenArtifact;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenMapResult;

import java.util.TreeMap;

public class BuildContext {
    public final boolean addDependenciesInfo;
    public final boolean allPluginsInfo;
    public final boolean addRemoteRepositoryInfo;
    public final boolean fullResourceInfo;
    public final boolean resultAsTree;
    public final boolean readOnly;
    public final MavenMapResult previousResult;
    public final TreeMap<String, MavenArtifact> readArtifactCache = new TreeMap<>();

    public BuildContext(
            boolean addDependenciesInfo,
            boolean allPluginsInfo,
            boolean addRemoteRepositoryInfo,
            boolean fullResourceInfo,
            boolean resultAsTree,
            MavenMapResult previousResult
    ) {
        this.addDependenciesInfo = addDependenciesInfo;
        this.allPluginsInfo = allPluginsInfo;
        this.addRemoteRepositoryInfo = addRemoteRepositoryInfo;
        this.fullResourceInfo = fullResourceInfo;
        this.resultAsTree = resultAsTree;
        this.readOnly = false;
        this.previousResult = previousResult;
    }

    public BuildContext(
            BuildContext context,
            boolean readOnly
    ) {
        this.addDependenciesInfo = context.addDependenciesInfo;
        this.allPluginsInfo = context.allPluginsInfo;
        this.addRemoteRepositoryInfo = context.addRemoteRepositoryInfo;
        this.fullResourceInfo = context.fullResourceInfo;
        this.resultAsTree = context.resultAsTree;
        this.readOnly = readOnly;
        this.previousResult = context.previousResult;
    }
}
