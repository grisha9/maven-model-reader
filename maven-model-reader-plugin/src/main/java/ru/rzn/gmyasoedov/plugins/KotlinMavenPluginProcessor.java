package ru.rzn.gmyasoedov.plugins;


import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import ru.rzn.gmyasoedov.util.PluginUtils;

import java.util.List;

public class KotlinMavenPluginProcessor implements PluginProcessor {

    @Override
    public String groupId() {
        return "org.jetbrains.kotlin";
    }

    @Override
    public String artifactId() {
        return "kotlin-maven-plugin";
    }

    @Override
    public void process(MavenProject project, Plugin plugin) {
        List<PluginExecution> executions = plugin.getExecutions();
        for (PluginExecution execution : executions) {
            List<String> goals = execution.getGoals();
            if (goals == null) continue;
            if (goals.contains("compile")) {
                List<String> pathList = PluginUtils.getPathList(execution.getConfiguration(), "sourceDirs");
                for (String path : pathList) {
                    project.addCompileSourceRoot(path);
                }
            }

            if (goals.contains("test-compile")) {
                List<String> pathList = PluginUtils.getPathList(execution.getConfiguration(), "sourceDirs");
                for (String path : pathList) {
                    project.addTestCompileSourceRoot(path);
                }
            }
        }
    }
}
