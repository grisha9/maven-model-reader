package ru.rzn.gmyasoedov.maven.plugin.reader;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import static org.apache.maven.plugins.annotations.LifecyclePhase.NONE;

@Mojo(
        name = "read",
        defaultPhase = NONE,
        aggregator = true,
        requiresDependencyResolution = ResolutionScope.NONE,
        threadSafe = true
)
public class ReadProjectModelMojo extends ResolveProjectModelMojo {

    @Override
    protected BuildContext getExecuteContext() {
        return new BuildContext(super.getExecuteContext(), true);
    }
}
