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

package org.apache.isis.config.resource;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceStreamSourceComposite extends ResourceStreamSourceAbstract {

    private static Logger LOG = LoggerFactory.getLogger(ResourceStreamSourceComposite.class);

    private final List<ResourceStreamSource> resourceStreamSources = new ArrayList<ResourceStreamSource>();

    public ResourceStreamSourceComposite(final ResourceStreamSource... resourceStreamSources) {
        for (final ResourceStreamSource rss : resourceStreamSources) {
            addResourceStreamSource(rss);
        }
    }

    public void addResourceStreamSource(final ResourceStreamSource rss) {
        this.resourceStreamSources.add(rss);
    }

    @Override
    protected InputStream doReadResource(final String resourcePath) {
        Vector<InputStream> compositionStreams = new Vector<InputStream>();
        for (final ResourceStreamSource rss : resourceStreamSources) {
            final InputStream resourceStream = rss.readResource(resourcePath);
            if (resourceStream != null) {
                compositionStreams.add(resourceStream);
            }
        }
        if (!compositionStreams.isEmpty()) {
            return new SequenceInputStream(compositionStreams.elements());
        }
        LOG.debug("could not load resource path '{}' from {}", resourcePath, getName());
        return null;
    }

    @Override
    public OutputStream writeResource(final String resourcePath) {
        return null; // No support for writing resources
    }

    @Override
    public String getName() {
        return "composite [" + resourceStreamNames() + "]";
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
