package ru.rzn.gmyasoedov.maven.plugin.reader;

import org.apache.maven.plugins.annotations.Mojo;

import static org.apache.maven.plugins.annotations.LifecyclePhase.NONE;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

@Mojo(name = "read", defaultPhase = NONE, aggregator = true, requiresDependencyResolution = TEST, threadSafe = true)
public class ReadProjectModelMojo extends ResolveProjectModelMojo {

    @Override
    protected BuildContext getExecuteContext() {
        return new BuildContext(super.getExecuteContext(), true);
    }
}
