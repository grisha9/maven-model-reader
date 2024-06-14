package ru.rzn.gmyasoedov.maven.plugin.reader.converter;

import org.apache.maven.artifact.repository.ArtifactRepository;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenRemoteRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RemoteRepositoryConverter {

    public static List<MavenRemoteRepository> convert(List<ArtifactRepository> remoteRepositories) {
        if (remoteRepositories == null || remoteRepositories.isEmpty()) return Collections.emptyList();
        ArrayList<MavenRemoteRepository> result = new ArrayList<>(remoteRepositories.size());
        for (ArtifactRepository each : remoteRepositories) {
            MavenRemoteRepository repository = new MavenRemoteRepository();
            repository.setId(each.getId());
            repository.setUrl(each.getUrl());
            result.add(repository);
        }
        return result;
    }
}
