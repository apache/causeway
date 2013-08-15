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

package org.apache.isis.core.commons.config;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.commons.resource.ResourceStreamSourceFileSystem;

/**
 * Convenience implementation of {@link IsisConfigurationBuilder} that loads
 * configuration resource as per {@link IsisConfigurationBuilderFileSystem} and
 * otherwise from the {@link ResourceStreamSourceContextLoaderClassPath
 * classpath}.
 * 
 * @see ResourceStreamSourceFileSystem
 */
public class IsisConfigurationBuilderDefault extends IsisConfigurationBuilderResourceStreams {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(IsisConfigurationBuilderDefault.class);

    public IsisConfigurationBuilderDefault() {
        super(resourceStreamSources(null));
    }

    public IsisConfigurationBuilderDefault(final String firstDirectory) {
        super(resourceStreamSources(firstDirectory));
    }

    private static ResourceStreamSource[] resourceStreamSources(final String firstDirectory) {
        final ArrayList<ResourceStreamSource> rssList = Lists.newArrayList();
        if (firstDirectory != null) {
            rssList.add(fromFileSystem(firstDirectory));
        }
        rssList.addAll(Arrays.asList(fromFileSystem(ConfigurationConstants.DEFAULT_CONFIG_DIRECTORY), fromFileSystem(ConfigurationConstants.WEBINF_FULL_DIRECTORY), fromClassPath(), fromClassPath(ConfigurationConstants.WEBINF_DIRECTORY)));
        return rssList.toArray(new ResourceStreamSource[0]);
    }

    private static ResourceStreamSource fromFileSystem(final String directory) {
        return ResourceStreamSourceFileSystem.create(directory);
    }

    private static ResourceStreamSource fromClassPath() {
        return ResourceStreamSourceContextLoaderClassPath.create();
    }

    private static ResourceStreamSource fromClassPath(final String prefix) {
        return ResourceStreamSourceContextLoaderClassPath.create(prefix);
    }

}
