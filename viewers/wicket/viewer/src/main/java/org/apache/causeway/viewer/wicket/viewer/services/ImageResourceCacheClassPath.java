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
package org.apache.causeway.viewer.wicket.viewer.services;

import java.io.ByteArrayInputStream;

import javax.annotation.Priority;
import javax.inject.Named;

import org.apache.wicket.Application;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PartWriterCallback;
import org.apache.wicket.request.resource.ResourceReference;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.wicket.model.models.ImageResourceCache;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;


/**
 * Caches images loaded either from the same package as the specified object,
 * or from the <tt>images</tt> package otherwise.
 * <p>
 * Searches for a fixed set of suffixes: <code>png, gif, jpeg, jpg, svg</code>.
 */
@Service
@Named("causeway.viewer.wicket.ImageResourceCacheClassPath")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("ClassPath")
//@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ImageResourceCacheClassPath
implements ImageResourceCache {

    private static final long serialVersionUID = 1L;

    @Override
    public ResourceReference resourceReferenceFor(final ManagedObject adapter) {
        val objectIcon = adapter.getIcon();
        return resourceReferenceForObjectIcon(objectIcon);
    }

    @Override
    public ResourceReference resourceReferenceForSpec(final ObjectSpecification spec) {
        val objectIcon = spec.getIcon(null);
        return resourceReferenceForObjectIcon(objectIcon);
    }

    // -- HELPER

    private ResourceReference resourceReferenceForObjectIcon(final ObjectIcon objectIcon) {
        return new ObjectIconResourceReference(objectIcon);
    }

    private static class ObjectIconResourceReference
    extends ResourceReference {

        private static final long serialVersionUID = 1L;

        private final @NonNull ObjectIconResource objectIconResource;

        public ObjectIconResourceReference(final ObjectIcon objectIcon) {
            super(new Key(Application.class.getName(), objectIcon.getIdentifier(), null, null, null));
            this.objectIconResource = new ObjectIconResource(objectIcon);
        }

        @Override
        public IResource getResource() {
            return objectIconResource;
        }

    }

    @RequiredArgsConstructor
    private static class ObjectIconResource
    extends AbstractResource {

        private static final long serialVersionUID = 1L;

        private final @NonNull ObjectIcon objectIcon;

        @Override
        protected ResourceResponse newResourceResponse(final Attributes attributes) {

            val imageDataBytes = objectIcon.asBytes();
            final long size = imageDataBytes.length;
            ResourceResponse resourceResponse = new ResourceResponse();
            resourceResponse.setContentType(objectIcon.getMimeType().getBaseType());
            resourceResponse.setAcceptRange(ContentRangeType.BYTES);
            resourceResponse.setContentLength(size);
            resourceResponse.setFileName(objectIcon.getShortName());
            RequestCycle cycle = RequestCycle.get();
            Long startbyte = cycle.getMetaData(CONTENT_RANGE_STARTBYTE);
            Long endbyte = cycle.getMetaData(CONTENT_RANGE_ENDBYTE);
            resourceResponse.setWriteCallback(
                new PartWriterCallback(
                        new ByteArrayInputStream(imageDataBytes),
                        size, startbyte, endbyte)
                .setClose(true));
            return resourceResponse;
        }

    }

}
