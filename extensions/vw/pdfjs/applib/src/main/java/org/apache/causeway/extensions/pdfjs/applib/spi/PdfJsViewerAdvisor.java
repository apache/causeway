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
package org.apache.causeway.extensions.pdfjs.applib.spi;

import java.io.Serializable;

import javax.annotation.Priority;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.extensions.pdfjs.applib.config.Scale;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;

/**
 * SPI service interface.
 *
 * @since 2.0 {@index}
 */
public interface PdfJsViewerAdvisor {

    /** The main SPI called by the viewer. */
    Advice advise(final InstanceKey instanceKey);

    /** Updates the service implementation whenever the user updates the page number,
     * for a particular object/property/user (ie ViewerKey). */
    void pageNumChangedTo(final InstanceKey instanceKey, final int pageNum);

    /** Updates the service implementation whenever the user updates the scale,
     * for a particular object/property/user (ie ViewerKey). */
    void scaleChangedTo(final InstanceKey instanceKey, final Scale scale);

    /** Updates the service implementation whenever the user updates the height,
     * for a particular object/property/user (ie ViewerKey). */
    void heightChangedTo(final InstanceKey instanceKey, final int height);

    /**
     * Value type that identifies an object's type and identifier,
     * its (PDF) property and the user that is viewing the object.
     * <p>
     * This is a (serializable) value type so that, for example,
     * implementations can use as a key within a hash structure.
     */
    @Programmatic
    @Value @RequiredArgsConstructor
    class InstanceKey implements Serializable {

        private static final long serialVersionUID = 1L;

        private final TypeKey typeKey;

        /**
         * The identifier of the object being rendered.
         * <p>
         * The {@link TypeKey#getLogicalTypeName() TypeKey#logicalTypeName}  and {@link #getIdentifier() identifier}
         * together constitute the object's identity (in effect, its {@link Bookmark}).
         */
        private final String identifier;

        public InstanceKey(
                final String logicalTypeName,
                final String identifier,
                final String propertyId,
                final String userName) {
            this(new TypeKey(logicalTypeName, propertyId, userName), identifier);
        }

        public Bookmark asBookmark() {
            return Bookmark.forLogicalTypeNameAndIdentifier(typeKey.logicalTypeName, identifier);
        }

        /**
         * Key for the rendering of a specific property of an object's type for an named individual.
         * <p>
         * This is a (serializable) value type so that, for example,
         * implementations can use as a key within a hash structure.
         */
        @Programmatic
        @Value @With
        public static class TypeKey implements Serializable {

            private static final long serialVersionUID = 1L;

            /**
             * The object type of the object being rendered.
             */
            private final String logicalTypeName;

            /**
             * The property of the object (a {@link Blob} containing a PDF) being rendered.
             */
            private final String propertyId;

            /**
             * The user for whom the object's property is being rendered.
             */
            private final String userName;

        }
    }

    /**
     * Immutable value type, that specifies the page number, scale and height to render the object with.
     * <p>
     * The <code>withXxx</code> allow clones of the value to be created,
     * for convenience of implementors.
     */
    @Programmatic
    @Value @With
    class Advice implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Integer pageNum;
        private final Scale scale;
        private final Integer height;
    }


    /**
     * Default implementation.
     */
    @Service
    @Priority(PriorityPrecedence.LATE)
    public static class Default implements PdfJsViewerAdvisor {

        @Override
        public Advice advise(InstanceKey instanceKey) {
            return new Advice(1, Scale._0_75, 800);
        }

        @Override
        public void pageNumChangedTo(InstanceKey instanceKey, int pageNum) {
        }

        @Override
        public void scaleChangedTo(InstanceKey instanceKey, Scale scale) {
        }

        @Override
        public void heightChangedTo(InstanceKey instanceKey, int height) {
        }
    }

}
