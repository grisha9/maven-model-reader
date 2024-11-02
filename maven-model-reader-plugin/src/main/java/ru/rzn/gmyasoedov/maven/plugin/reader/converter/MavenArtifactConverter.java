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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import ru.rzn.gmyasoedov.maven.plugin.reader.model.MavenArtifact;

import static ru.rzn.gmyasoedov.maven.plugin.reader.util.ObjectUtils.emptyStringIfNull;


public class MavenArtifactConverter {
    public static MavenArtifact convert(Artifact artifact) {
        MavenArtifact result = new MavenArtifact();
        result.setGroupId(emptyStringIfNull(artifact.getGroupId()));
        result.setArtifactId(emptyStringIfNull(artifact.getArtifactId()));
        String version = artifact.getBaseVersion() != null ? artifact.getBaseVersion() : artifact.getVersion();
        result.setVersion(emptyStringIfNull(version));
        result.setType(artifact.getType());
        result.setClassifier(artifact.getClassifier());
        result.setScope(artifact.getScope());
        result.setOptional(artifact.isOptional());
        result.setFilePath(artifact.getFile() != null ? artifact.getFile().getAbsolutePath() : null);
        result.setResolved(artifact.isResolved());
        return result;
    }

    public static MavenArtifact convert(MavenProject project) {
        MavenArtifact result = new MavenArtifact();
        result.setGroupId(project.getGroupId());
        result.setArtifactId(project.getArtifactId());
        result.setVersion(project.getVersion());
        result.setType(project.getPackaging());
        result.setFilePath(project.getFile() != null ? project.getFile().getAbsolutePath() : null);
        result.setResolved(true);
        return result;
    }
}
