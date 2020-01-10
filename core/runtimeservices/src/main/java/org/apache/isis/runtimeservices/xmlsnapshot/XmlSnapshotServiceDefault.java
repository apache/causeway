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
package org.apache.isis.runtimeservices.xmlsnapshot;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.xmlsnapshot.XmlSnapshotService;
import org.apache.isis.applib.services.xmlsnapshot.XmlSnapshotServiceAbstract;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.util.snapshot.XmlSnapshot;

/**
 * This service allows an XML document to be generated capturing the data of a root entity and specified related
 * entities.  This XML can be used for various purposes, such as mail merge/reporting, or adhoc auditing.
 *
 * <p>
 * This implementation has no UI and there are no other implementations of the service API, and so it annotated
 * with {@link org.apache.isis.applib.annotation.DomainService}.  Because this class is implemented in core, this means
 * that it is automatically registered and available for use; no further configuration is required.
 */
@Service
@Named("isisRuntimeServices.XmlSnapshotServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class XmlSnapshotServiceDefault extends XmlSnapshotServiceAbstract {
    
    @Inject private SpecificationLoader specificationLoader;

    static class XmlSnapshotServiceDefaultBuilder implements XmlSnapshotService.Builder{

        private final XmlSnapshotBuilder builder;
        public XmlSnapshotServiceDefaultBuilder(SpecificationLoader specificationLoader, Object domainObject) {
            builder = new XmlSnapshotBuilder(specificationLoader, domainObject);
        }

        @Override
        public void includePath(String path) {
            builder.includePath(path);
        }

        @Override
        public void includePathAndAnnotation(String path, String annotation) {
            builder.includePathAndAnnotation(path, annotation);
        }

        @Override
        public XmlSnapshotService.Snapshot build() {
            XmlSnapshot xmlSnapshot = builder.build();
            return xmlSnapshot;
        }
    }

    /**
     * Creates a simple snapshot of the domain object.
     */
    @Override
    public XmlSnapshotService.Snapshot snapshotFor(final Object domainObject) {
        final ManagedObject adapter = ManagedObject.of(specificationLoader::loadSpecification, domainObject);
        return new XmlSnapshot(adapter);
    }

    /**
     * Creates a builder that allows a custom snapshot - traversing additional associated
     * properties or collections (using {@link Builder#includePath(String)} and
     * {@link Builder#includePathAndAnnotation(String, String)}) - to be created.
     */
    @Override
    public Builder builderFor(final Object domainObject) {
        return new XmlSnapshotServiceDefaultBuilder(specificationLoader, domainObject);
    }


}
