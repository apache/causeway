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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceStreamSourceChainOfResponsibility extends ResourceStreamSourceAbstract {

    private static Logger LOG = LoggerFactory.getLogger(ResourceStreamSourceChainOfResponsibility.class);

    private final List<ResourceStreamSource> resourceStreamSources = new ArrayList<ResourceStreamSource>();

    public ResourceStreamSourceChainOfResponsibility(final ResourceStreamSource... resourceStreamSources) {
        for (final ResourceStreamSource rss : resourceStreamSources) {
            addResourceStreamSource(rss);
        }
    }

    public void addResourceStreamSource(final ResourceStreamSource rss) {
        if(rss == null) {
            return;
        }
        this.resourceStreamSources.add(rss);
    }

    @Override
    protected InputStream doReadResource(final String resourcePath) {
        for (final ResourceStreamSource rss : resourceStreamSources) {
            final InputStream resourceStream = rss.readResource(resourcePath);
            if (resourceStream != null) {
                return resourceStream;
            }
        }
        LOG.debug("could not load resource path '{}' from {}", resourcePath, getName());
        return null;
    }

    @Override
    public OutputStream writeResource(final String resourcePath) {
        for (final ResourceStreamSource rss : resourceStreamSources) {
            final OutputStream os = rss.writeResource(resourcePath);
            if (os != null) {
                return os;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "chain [" + resourceStreamNames() + "]";
    }

    private String resourceStreamNames() {
        final StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (final ResourceStreamSource rss : resourceStreamSources) {
            if (first) {
                first = false;
            } else {
                buf.append(", ");
            }
            buf.append(rss.getName());
        }
        return buf.toString();
    }

}
