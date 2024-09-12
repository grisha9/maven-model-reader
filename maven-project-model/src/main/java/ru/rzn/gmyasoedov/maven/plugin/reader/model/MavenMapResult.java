
package ru.rzn.gmyasoedov.maven.plugin.reader.model;

import java.util.Collections;
import java.util.List;

public class MavenMapResult {
    public  boolean pluginNotResolved;
    public  MavenSettings settings;
    public  MavenProjectContainer container;
    public  List<String> exceptions = Collections.emptyList();
}
