package ru.rzn.gmyasoedov.maven.plugin.reader.plugins;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import ru.rzn.gmyasoedov.maven.plugin.reader.util.MavenContextUtils;
import ru.rzn.gmyasoedov.maven.plugin.reader.util.MavenJDOMUtil;
import ru.rzn.gmyasoedov.maven.plugin.reader.util.PluginUtils;

import java.util.List;
import java.util.Objects;

import static ru.rzn.gmyasoedov.maven.plugin.reader.util.PluginUtils.getAbsolutePath;

public class ApacheMavenCompilerPluginProcessor implements PluginProcessor {

    public static final String GROUP_ID = "org.apache.maven.plugins";
    public static final String ARTIFACT_ID = "maven-compiler-plugin";

    @Override
    public String groupId() {
        return GROUP_ID;
    }

    @Override
    public String artifactId() {
        return ARTIFACT_ID;
    }

    @Override
    public void process(MavenProject project, Plugin plugin) {
        Xpp3Dom configuration = PluginUtils.getConfiguration(plugin, "compile");
        String srcPath = MavenJDOMUtil.findChildValueByPath(configuration, "generatedSourcesDirectory", "");

        Xpp3Dom testConfiguration = PluginUtils.getConfiguration(plugin, "test-compile");
        String testPath = MavenJDOMUtil.findChildValueByPath(testConfiguration, "generatedTestSourcesDirectory", "");
        if (!srcPath.isEmpty()) {
            MavenContextUtils.addStringValue(
                    project, MavenContextUtils.GENERATED_PATH, PluginUtils.getAbsolutePath(srcPath, project.getBuild().getDirectory())
            );
        }
        if (!testPath.isEmpty()) {
            MavenContextUtils.addStringValue(
                    project, MavenContextUtils.GENERATED_TEST_PATH, PluginUtils.getAbsolutePath(testPath, project.getBuild().getDirectory())
            );
        }
        applyGroovyProcessor(project, plugin);
    }

    private static void applyGroovyProcessor(MavenProject project, Plugin plugin) {
        List<Dependency> dependencies = plugin.getDependencies();
        if (dependencies == null || dependencies.isEmpty()) return;
        boolean isGroovy = false;
        for (Dependency each : dependencies) {
            if (Objects.equals(each.getGroupId(), "org.codehaus.groovy")
                    && Objects.equals(each.getArtifactId(), "groovy-eclipse-compiler")) {
                isGroovy = true;
            }
        }
        if (!isGroovy) return;
        AbstractGroovyPluginProcessor.process(project, plugin);
    }
}
