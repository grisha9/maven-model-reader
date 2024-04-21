
package ru.rzn.gmyasoedov.model;

import java.util.Collections;
import java.util.List;

public class MavenMapResult {
    public  boolean pluginNotResolved;
    public  MavenSettings settings;
    public  MavenProjectContainer container;
    public  List<MavenException> exceptions = Collections.emptyList();
}
