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

package org.apache.isis.metamodel.config;

import org.apache.isis.commons.resource.ResourceStreamSource;
import org.apache.isis.commons.resource.ResourceStreamSourceComposite;
import org.apache.isis.commons.resource.ResourceStreamSourceFileSystem;
import org.apache.log4j.Logger;

/**
 * Convenience implementation of {@link ConfigurationBuilder} that loads configuration resource from a specified
 * directory (or directories) on the filesystem.
 * 
 * @see ResourceStreamSourceFileSystem
 */
public class ConfigurationBuilderFileSystem extends ConfigurationBuilderResourceStreams {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ConfigurationBuilderFileSystem.class);

    private static ResourceStreamSource createResourceStreamSource(String... directories) {
        ResourceStreamSourceComposite composite = new ResourceStreamSourceComposite();
        for (String directory : directories) {
            composite.addResourceStreamSource(new ResourceStreamSourceFileSystem(directory));
        }
        return composite;
    }

    public ConfigurationBuilderFileSystem(String... directories) {
        super(createResourceStreamSource(directories));
    }

    public ConfigurationBuilderFileSystem() {
        super(new ResourceStreamSourceFileSystem(ConfigurationConstants.DEFAULT_CONFIG_DIRECTORY));
    }

}
