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
import java.util.Collection;
import java.util.Collections;

public class MavenSettings implements Serializable {
    public int modulesCount;
    public String localRepository;
    public String settingsPath;
    public Collection<MavenProfile> profiles = Collections.emptyList();
    public Collection<MavenRemoteRepository> remoteRepositories = Collections.emptyList();

    public int getModulesCount() {
        return modulesCount;
    }

    public void setModulesCount(int modulesCount) {
        this.modulesCount = modulesCount;
    }

    public String getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(String localRepository) {
        this.localRepository = localRepository;
    }

    public String getSettingsPath() {
        return settingsPath;
    }

    public void setSettingsPath(String settingsPath) {
        this.settingsPath = settingsPath;
    }

    public Collection<MavenProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Collection<MavenProfile> profiles) {
        this.profiles = profiles;
    }

    public Collection<MavenRemoteRepository> getRemoteRepositories() {
        return remoteRepositories;
    }

    public void setRemoteRepositories(Collection<MavenRemoteRepository> remoteRepositories) {
        this.remoteRepositories = remoteRepositories;
    }
}
