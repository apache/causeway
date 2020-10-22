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
package org.apache.isis.viewer.wicket.ui.components.scalars.blobclob;

import java.util.UUID;

import org.apache.wicket.IRequestListener;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.IResource.Attributes;

/**
 * Each ResourceLinkVolatile instance generates a unique URL, which 
 * effectively eliminates any caching during the request response cycle.
 * <p>
 * This is the desired behavior for Blob/Clob 'download' buttons.
 * 
 * @since 2.0
 * @implNote this is almost a copy of Wicket's {@link ResourceLink}.
 */
public class ResourceLinkVolatile extends Link<Object> implements IRequestListener {
    private static final long serialVersionUID = 1L;

    /** The Resource */
    private final IResource resource;

    /** The resource parameters */
    private final PageParameters resourceParameters;

    /**
     * Constructs a link directly to the provided resource.
     * 
     * @param id       See Component
     * @param resource The resource
     */
    public ResourceLinkVolatile(final String id, final IResource resource) {
        super(id);
        this.resource = resource;
        this.resourceParameters = new PageParameters()
                .add("antiCache", UUID.randomUUID().toString()); 
    }

    @Override
    public void onClick() {
    }

    @Override
    public boolean rendersPage() {
        return false;
    }

    @Override
    protected boolean getStatelessHint() {
        return false;
    }

    @Override
    protected final CharSequence getURL() {
        return urlForListener(resourceParameters);
    }

    @Override
    public final void onRequest() {
        Attributes a = new Attributes(RequestCycle.get().getRequest(), RequestCycle.get().getResponse(), null);
        resource.respond(a);

        super.onRequest();
    }
}
