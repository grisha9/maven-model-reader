package ru.rzn.gmyasoedov.model;

import java.io.Serializable;

public class MavenException {
    public String message;
    public MavenId mavenId;
    public String projectFilePath;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MavenId getMavenId() {
        return mavenId;
    }

    public void setMavenId(MavenId mavenId) {
        this.mavenId = mavenId;
    }

    public String getProjectFilePath() {
        return projectFilePath;
    }

    public void setProjectFilePath(String projectFilePath) {
        this.projectFilePath = projectFilePath;
    }
}
