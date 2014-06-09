/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.runtime.installerregistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.isis.core.commons.components.Installer;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.runtime.about.AboutIsis;
import org.apache.isis.core.runtime.about.ComponentDetails;

/**
 * Details name and version of installer.
 */
public class InstallerVersion implements ComponentDetails {

    private final Installer installer;

    public InstallerVersion(final Installer installer) {
        this.installer = installer;
    }

    @Override
    public String getName() {
        return installer.getName();
    }

    @Override
    public String getModule() {
        return "org.apache.isis.plugins:dndviewer";
    }

    @Override
    public String getVersion() {
        return findVersion(getModule());
    }

    @Override
    public boolean isInstalled() {
        return false;
    }

    private String findVersion(final String moduleId) {
        try {
            final String module = moduleId.replace(":", "/");
            final InputStream resourceAsStream = AboutIsis.class.getClassLoader().getResourceAsStream("META-INF/maven/" + module + "/pom.properties");
            if (resourceAsStream == null) {
                return "no version";
            }
            final Properties p = new Properties();
            p.load(resourceAsStream);
            final String version = p.getProperty("version");
            return version;
        } catch (final IOException e) {
            throw new IsisException(e);
        }
    }

}
