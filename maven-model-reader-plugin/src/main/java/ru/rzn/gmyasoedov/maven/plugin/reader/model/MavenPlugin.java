
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
import java.util.Objects;

public final class MavenPlugin implements MavenId, Serializable {

    private String artifactId;
    private String groupId;
    private String version;
    private PluginBody body;

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public PluginBody getBody() {
        return body;
    }

    public void setBody(PluginBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "MavenPlugin{" +
                "groupId=" + groupId + '\'' +
                ", artifactId=" + artifactId + '\'' +
                ", version=" + version + '\'' +
                ", body=" + body +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MavenPlugin that = (MavenPlugin) o;
        return Objects.equals(artifactId, that.artifactId) && Objects.equals(groupId, that.groupId) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(artifactId);
        result = 31 * result + Objects.hashCode(groupId);
        result = 31 * result + Objects.hashCode(version);
        return result;
    }
}
