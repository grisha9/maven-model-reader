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
