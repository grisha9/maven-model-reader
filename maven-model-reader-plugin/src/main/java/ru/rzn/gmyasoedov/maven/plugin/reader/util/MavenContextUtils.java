/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package ru.rzn.gmyasoedov.maven.plugin.reader.util;

import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.List;

public abstract class MavenContextUtils {
    public static final String ANNOTATION_PROCESSOR_PATHS = "GMaven:annotationProcPaths";
    public static final String EXCLUDED_PATHS = "GMaven:excludedPaths";
    public static final String GENERATED_PATH = "GMaven:generatedPath";
    public static final String GENERATED_TEST_PATH = "GMaven:generatedTestPath";

    public static void addStringValue(MavenProject project, String key, String value) {
        project.setContextValue(key, value);
    }

    public static void addListStringValue(MavenProject project, String key, String value) {
        List<String> container = getListContainer(project, key);
        container.add(value);
    }

    public static void addListStringValues(MavenProject project, String key, List<String> values) {
        List<String> container = getListContainer(project, key);
        container.addAll(values);
    }

    private static List<String> getListContainer(MavenProject project, String key) {
        Object contextValue = project.getContextValue(key);
        if (contextValue instanceof ArrayList) {
            return (List<String>) contextValue;
        } else {
            List<String> list = new ArrayList<String>(1);
            project.setContextValue(key, list);
            return list;
        }
    }
}
