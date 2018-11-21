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

package org.apache.isis.core.commons.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ResourceStreamSourceAbstract implements ResourceStreamSource {

    private static Logger LOG = LoggerFactory.getLogger(ResourceStreamSourceAbstract.class);

    @Override
    public final InputStream readResource(final String resourcePath) {
        try {
            final InputStream resourceStream = doReadResource(resourcePath);
            if (resourceStream != null) {
                return resourceStream;
            }
            LOG.debug("could not load resource path '{}' from {}", resourcePath, getName());
        } catch (final IOException e) {
            LOG.debug("could not load resource path '{}' from {}, exception: {}", resourcePath, getName(), e.getMessage());
        }
        return null;
    }

    /**
     * Mandatory hook method; subclasses can return either <tt>null</tt> or
     * throw an exception if the resource could not be found.
     */
    protected abstract InputStream doReadResource(String resourcePath) throws IOException;

    /**
     * Default implementation returns <tt>null</tt> (that is, not supported).
     */
    @Override
    public OutputStream writeResource(final String resourcePath) {
        return null;
    }

    @Override
    public String toString() {
        return getName();
    }
}
