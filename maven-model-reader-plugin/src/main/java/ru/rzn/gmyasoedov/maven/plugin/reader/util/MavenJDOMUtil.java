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

import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MavenJDOMUtil {

    public static Xpp3Dom findChildByPath(Xpp3Dom element, String path) {
        int i = 0;
        while (element != null) {
            int dot = path.indexOf('.', i);
            if (dot == -1) {
                return element.getChild(path.substring(i));
            }

            element = element.getChild(path.substring(i, dot));
            i = dot + 1;
        }

        return null;
    }

    public static String findChildValueByPath(Xpp3Dom element, String path, String defaultValue) {
        Xpp3Dom child = findChildByPath(element, path);
        if (child == null) return defaultValue;
        String childValue = child.getValue();
        if (childValue == null) return defaultValue;
        return childValue.isEmpty() ? defaultValue : childValue;
    }

    public static String findChildValueByPath(Xpp3Dom element, String path) {
        return findChildValueByPath(element, path, null);
    }

    public static boolean hasChildByPath(Xpp3Dom element, String path) {
        return findChildByPath(element, path) != null;
    }

    public static List<Xpp3Dom> findChildrenByPath(Xpp3Dom element, String path, String subPath) {
        return collectChildren(findChildByPath(element, path), subPath);
    }

    public static List<String> findChildrenValuesByPath(Xpp3Dom element, String path, String childrenName) {
        List<String> result = new ArrayList<>();
        for (Xpp3Dom each : findChildrenByPath(element, path, childrenName)) {
            String value = each.getValue();
            if (value != null && !value.isEmpty()) {
                result.add(value);
            }
        }
        return result;
    }

    private static List<Xpp3Dom> collectChildren(Xpp3Dom container, String subPath) {
        if (container == null) return Collections.emptyList();

        int firstDot = subPath.indexOf('.');

        if (firstDot == -1) {
            Xpp3Dom[] children = container.getChildren(subPath);
            return (children == null || children.length == 0)
                    ? Collections.<Xpp3Dom>emptyList() : Arrays.asList(children);
        }

        String childName = subPath.substring(0, firstDot);
        String pathInChild = subPath.substring(firstDot + 1);

        List<Xpp3Dom> result = new ArrayList<>();

        for (Xpp3Dom each : container.getChildren(childName)) {
            Xpp3Dom child = findChildByPath(each, pathInChild);
            if (child != null) result.add(child);
        }
        return result;
    }
}
