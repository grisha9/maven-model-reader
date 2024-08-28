package ru.rzn.gmyasoedov.dependency.graph;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.graph.DependencyNode;

import javax.inject.Named;
import java.util.List;

@Named
public class DependencyResolutionResultEventSpy extends AbstractEventSpy {

    private MavenSession session;

    @Override
    public void onEvent(Object event) {
        if (event instanceof ExecutionEvent) {
            if (((ExecutionEvent) event).getSession() != null) {
                session = ((ExecutionEvent) event).getSession();
            }
        } else if (event instanceof DependencyResolutionResult) {
            DependencyNode dependencyGraph = ((DependencyResolutionResult) event).getDependencyGraph();
            setGraphToProjectContext(dependencyGraph);
        }
    }

    private void setGraphToProjectContext(DependencyNode dependencyGraph) {
        if (session == null) return;
        String key = dependencyGraph.getArtifact().getArtifactId();
        if (key == null) return;
        List<MavenProject> projects = session.getProjects();
        if (projects == null) return;
        for (MavenProject project : projects) {
            if (key.equals(project.getArtifactId())) {
                project.setContextValue("gmaven-dependency-graph", dependencyGraph);
                return;
            }
        }
    }
}
