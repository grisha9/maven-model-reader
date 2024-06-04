package ru.rzn.gmyasoedov.converter;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import ru.rzn.gmyasoedov.BuildContext;
import ru.rzn.gmyasoedov.model.MavenArtifact;
import ru.rzn.gmyasoedov.model.MavenPlugin;
import ru.rzn.gmyasoedov.model.PluginBody;
import ru.rzn.gmyasoedov.model.PluginExecution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MavenPluginConverter {
    public static MavenPlugin convert(Plugin plugin, MavenProject mavenProject) {
        Object contextValue = mavenProject
                .getContextValue("gPlugin:" + plugin.getGroupId() + ":" + plugin.getArtifactId());
        if (contextValue == null) return null;
        MavenPlugin mavenPlugin = new MavenPlugin();
        mavenPlugin.setGroupId(plugin.getGroupId());
        mavenPlugin.setArtifactId(plugin.getArtifactId());
        mavenPlugin.setVersion(plugin.getVersion());
        mavenPlugin.setBody(getPluginBody(contextValue, plugin.getDependencies()));
        return mavenPlugin;
    }

    private static PluginBody getPluginBody(Object contextValue, List<Dependency> dependencies) {
        if (contextValue instanceof Map) {
            try {
                Map<String, Object> map = (Map<String, Object>) contextValue;
                List<Map<String, Object>> executions = (List<Map<String, Object>>) map.get("executions");
                PluginBody pluginBody = new PluginBody();
                pluginBody.setExecutions(mapToExecutions(executions));
                pluginBody.setDependencies(toArtifactList(dependencies));
                pluginBody.setConfiguration(getConfiguration(map));
                return pluginBody;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private static List<PluginExecution> mapToExecutions(List<Map<String, Object>> executions) {
        if (executions == null) Collections.emptyList();
        ArrayList<PluginExecution> result = new ArrayList<>(executions.size());
        for (Map<String, Object> execution : executions) {
            List<String> goals = (List<String>) execution.get("goals");
            PluginExecution pluginExecution = new PluginExecution();
            pluginExecution.setId((String) execution.get("id"));
            pluginExecution.setPhase((String) execution.get("phase"));
            pluginExecution.setGoals(goals == null ? Collections.<String>emptyList() : new ArrayList<>(goals));
            pluginExecution.setConfiguration(getConfiguration(execution));
            result.add(pluginExecution);
        }
        return result;
    }

    private static String getConfiguration(Map<String, Object> map) {
        if (map == null) return null;
        Object configuration = map.get("configuration");
        return configuration instanceof String ? (String) configuration : null;
    }

    private static List<MavenArtifact> toArtifactList(List<Dependency> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) return null;
        ArrayList<MavenArtifact> result = new ArrayList<>(dependencies.size());
        for (Dependency each : dependencies) {
            MavenArtifact artifact = new MavenArtifact();
            artifact.setGroupId(each.getGroupId());
            artifact.setArtifactId(each.getArtifactId());
            artifact.setVersion(each.getVersion());
            artifact.setType(each.getType());
            artifact.setClassifier(each.getClassifier());
            artifact.setScope(each.getScope());
            artifact.setOptional(each.isOptional());
            result.add(artifact);
        }
        return result;
    }
}
