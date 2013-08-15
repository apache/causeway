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

package org.apache.isis.core.webapp.config;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderFileSystem;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderResourceStreams;

/**
 * Convenience implementation of {@link IsisConfigurationBuilder} that loads
 * configuration resource using the {@link ResourceStreamSourceForWebInf}.
 */
public class IsisConfigurationBuilderForWebapp extends IsisConfigurationBuilderResourceStreams {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(IsisConfigurationBuilderFileSystem.class);

    public IsisConfigurationBuilderForWebapp(final ServletContext servletContext) {
        super(new ResourceStreamSourceForWebInf(servletContext));
    }

}
