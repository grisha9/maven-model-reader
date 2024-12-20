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
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenProfile;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenSettings;

import java.io.File;
import java.util.*;

public class MavenSettingsConverter {

    public static MavenSettings convert(MavenSession session) {
        if (session == null) {
            return new MavenSettings();
        }
        String localRepository = session.getRequest().getLocalRepository().getBasedir();
        File settingsFile = session.getRequest().getUserSettingsFile();
        String settingsFilePath = settingsFile == null ? null : settingsFile.getAbsolutePath();
        Collection<String> activeProfiles = getActiveSettingsProfiles(session);

        MavenSettings settings = new MavenSettings();
        settings.setModulesCount(session.getAllProjects() == null ? 0 : session.getAllProjects().size());
        settings.setLocalRepository(localRepository);
        settings.setSettingsPath(settingsFilePath);
        settings.setProfiles(getMavenProfiles(session, activeProfiles));
        settings.setRemoteRepositories(RemoteRepositoryConverter.convert(session.getRequest().getRemoteRepositories()));
        return settings;
    }

    private static Collection<MavenProfile> getMavenProfiles(MavenSession session,
                                                             Collection<String> activeProfiles) {
        Set<MavenProfile> profiles = new HashSet<>(session.getRequest().getProfiles().size() * 2);
        for (Profile profile : session.getRequest().getProfiles()) {
            profiles.add(createMavenProfile(profile, activeProfiles));
        }
        if (session.getProjects() != null) {
            for (MavenProject project : session.getProjects()) {
                for (Profile profile : project.getModel().getProfiles()) {
                    profiles.add(createMavenProfile(profile, activeProfiles));
                }
            }
        }
        return profiles;
    }

    private static MavenProfile createMavenProfile(Profile profile, Collection<String> activeProfiles) {
        MavenProfile mavenProfile = new MavenProfile();
        mavenProfile.setName(profile.getId());
        mavenProfile.setActivation(profile.getActivation() != null || activeProfiles.contains(profile.getId()));
        return mavenProfile;
    }

    private static List<String> getActiveSettingsProfiles(MavenSession session) {
        List<String> activeProfiles = session.getRequest().getActiveProfiles();
        if (activeProfiles == null || activeProfiles.isEmpty()) return Collections.emptyList();
        List<Profile> profiles = session.getRequest().getProfiles();
        profiles = profiles == null ? Collections.<Profile>emptyList() : profiles;
        ArrayList<String> activeSettingsProfile = new ArrayList<>(3);
        for (Profile profile : profiles) {
            if ("settings.xml".equalsIgnoreCase(profile.getSource()) && activeProfiles.contains(profile.getId())) {
                activeSettingsProfile.add(profile.getId());
            }
        }
        return activeSettingsProfile;
    }

}
