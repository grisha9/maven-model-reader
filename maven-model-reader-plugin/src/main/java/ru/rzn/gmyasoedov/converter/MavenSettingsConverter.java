package ru.rzn.gmyasoedov.converter;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import ru.rzn.gmyasoedov.model.MavenProfile;
import ru.rzn.gmyasoedov.model.MavenRemoteRepository;
import ru.rzn.gmyasoedov.model.MavenSettings;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MavenSettingsConverter {

    public static MavenSettings convert(MavenSession session) {
        if (session == null) {
            return new MavenSettings();
        }
        String localRepository = session.getRequest().getLocalRepository().getBasedir();
        File settingsFile = session.getRequest().getUserSettingsFile();
        String settingsFilePath = settingsFile == null ? null : settingsFile.getAbsolutePath();
        Collection<String> activeProfiles = session.getRequest().getActiveProfiles();//todo

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
}
