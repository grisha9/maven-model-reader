
package ru.rzn.gmyasoedov.maven.plugin.reader.model;

public final class MavenPlugin implements MavenId {

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
                ", groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", body=" + body +
                '}';
    }
}
