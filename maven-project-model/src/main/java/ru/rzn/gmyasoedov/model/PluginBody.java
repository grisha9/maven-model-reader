package ru.rzn.gmyasoedov.model;


import java.util.Collections;
import java.util.List;

public class PluginBody {
    private List<PluginExecution> executions = Collections.emptyList();
    private List<String> annotationProcessorPaths = Collections.emptyList();
    private List<MavenArtifact> dependencies = Collections.emptyList();
    private String configuration;

    public List<PluginExecution> getExecutions() {
        return executions;
    }

    public void setExecutions(List<PluginExecution> executions) {
        this.executions = executions;
    }

    public List<String> getAnnotationProcessorPaths() {
        return annotationProcessorPaths;
    }

    public void setAnnotationProcessorPaths(List<String> annotationProcessorPaths) {
        this.annotationProcessorPaths = annotationProcessorPaths;
    }

    public List<MavenArtifact> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<MavenArtifact> dependencies) {
        this.dependencies = dependencies;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return "PluginBody{" +
                "executions=" + executions +
                ", annotationProcessorPaths=" + annotationProcessorPaths +
                ", dependencies=" + dependencies +
                ", configuration='" + configuration + '\'' +
                '}';
    }
}
