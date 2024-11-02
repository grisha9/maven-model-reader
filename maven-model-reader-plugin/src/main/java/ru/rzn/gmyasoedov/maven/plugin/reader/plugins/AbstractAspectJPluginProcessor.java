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

package ru.rzn.gmyasoedov.maven.plugin.reader.plugins;


import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import ru.rzn.gmyasoedov.maven.plugin.reader.util.MavenJDOMUtil;
import ru.rzn.gmyasoedov.maven.plugin.reader.util.PluginUtils;

import java.nio.file.Paths;

public abstract class AbstractAspectJPluginProcessor {

    public static void process(MavenProject project, Plugin plugin) {
        Xpp3Dom configuration = PluginUtils.getConfiguration(plugin, "compile");
        String srcPath = MavenJDOMUtil.findChildValueByPath(
                configuration, "aspectDirectory", Paths.get("src", "main", "aspect").toString()
        );
        Xpp3Dom testConfiguration = PluginUtils.getConfiguration(plugin, "test-compile");
        String testPath = MavenJDOMUtil.findChildValueByPath(
                testConfiguration, "testAspectDirectory", Paths.get("src", "test", "aspect").toString()
        );
        if (!srcPath.isEmpty()) {
            project.addCompileSourceRoot(srcPath);
        }
        if (!testPath.isEmpty()) {
            project.addTestCompileSourceRoot(testPath);
        }
    }
}
