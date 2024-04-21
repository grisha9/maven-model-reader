package ru.rzn.gmyasoedov.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class MavenListResult {
    public  boolean pluginNotResolved;
    public  MavenSettings settings;
    public  List<MavenProject> mavenProjects = Collections.emptyList();
    public  List<MavenException> exceptions = Collections.emptyList();
}
