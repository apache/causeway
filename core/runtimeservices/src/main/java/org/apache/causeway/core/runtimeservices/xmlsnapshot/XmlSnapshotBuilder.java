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
package org.apache.causeway.core.runtimeservices.xmlsnapshot;

import java.util.List;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.util.snapshot.XmlSchema;
import org.apache.causeway.core.metamodel.util.snapshot.XmlSnapshot;

import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Builds a {@link XmlSnapshot} fluently with a builder:
 *
 * <pre>
 * XmlSnapshot snapshot = XmlSnapshotBuilder
 *     .create(customer)
 *     .includePath(&quot;placeOfBirth&quot;)
 *     .includePath(&quot;orders/product&quot;)
 *     .build();
 * Element customerAsXml = snapshot.toXml();
 * </pre>
 *
 * @since 1.0 {@index}
 */
@RequiredArgsConstructor
public class XmlSnapshotBuilder {

    private final @NonNull SpecificationLoader specificationLoader;

    /** required, must also be a scalar */
    private final @NonNull Object domainObject;
    private @Nullable XmlSchema schema;

    static class PathAndAnnotation {
        public PathAndAnnotation(final String path, final String annotation) {
            this.path = path;
            this.annotation = annotation;
        }

        private final String path;
        private final String annotation;
    }

    private final List<XmlSnapshotBuilder.PathAndAnnotation> paths = _Lists.newArrayList();

    public XmlSnapshotBuilder usingSchema(final XmlSchema schema) {
        this.schema = schema;
        return this;
    }

    public XmlSnapshotBuilder includePath(final String path) {
        return includePathAndAnnotation(path, null);
    }

    public XmlSnapshotBuilder includePathAndAnnotation(final String path, final String annotation) {
        paths.add(new PathAndAnnotation(path, annotation));
        return this;
    }

    public XmlSnapshot build() {
        final ManagedObject adapter = ManagedObject.adaptSingular(specificationLoader, domainObject);
        final XmlSnapshot snapshot = (schema != null)
                ? new XmlSnapshot(adapter, schema)
                : new XmlSnapshot(adapter);
        for (final XmlSnapshotBuilder.PathAndAnnotation paa : paths) {
            if (paa.annotation != null) {
                snapshot.include(paa.path, paa.annotation);
            } else {
                snapshot.include(paa.path);
            }
        }
        return snapshot;
    }

}
