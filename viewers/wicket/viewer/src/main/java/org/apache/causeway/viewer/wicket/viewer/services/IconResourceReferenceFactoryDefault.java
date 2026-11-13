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

import jakarta.inject.Named;

import org.apache.wicket.Application;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PartWriterCallback;
import org.apache.wicket.request.resource.ResourceReference;
import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.render.ObjectIconUrlBased;
import org.apache.causeway.viewer.wicket.model.models.IconResourceReferenceFactory;
import org.apache.causeway.viewer.wicket.viewer.CausewayModuleViewerWicketViewer;

import lombok.RequiredArgsConstructor;

@Service
@Named(CausewayModuleViewerWicketViewer.NAMESPACE + ".IconResourceReferenceFactoryDefault")
public record IconResourceReferenceFactoryDefault()
implements IconResourceReferenceFactory {

    @Override
    public ResourceReference resourceReferenceForObjectIcon(final ObjectIconUrlBased objectIcon) {
        return new ObjectIconResourceReference(objectIcon);
    }

    // -- HELPER

    private static class ObjectIconResourceReference
    extends ResourceReference {

        private static final long serialVersionUID = 1L;

        private final @NonNull ObjectIconResource objectIconResource;

        public ObjectIconResourceReference(final ObjectIconUrlBased objectIcon) {
            super(new Key(Application.class.getName(), objectIcon.cacheId(), null, null, null));
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

        private final @NonNull ObjectIconUrlBased objectIcon;

        @Override
        protected ResourceResponse newResourceResponse(final Attributes attributes) {
            var imageDataBytes = objectIcon.iconData();
            final long size = imageDataBytes.length;
            var resourceResponse = new ResourceResponse();
            resourceResponse.setContentType(objectIcon.mediaType());
            resourceResponse.setAcceptRange(ContentRangeType.BYTES);
            resourceResponse.setContentLength(size);
            resourceResponse.setFileName(objectIcon.shortName());
            var cycle = RequestCycle.get();
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
