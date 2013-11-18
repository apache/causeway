/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.snapshot;

import org.apache.isis.applib.services.xmlsnapshot.XmlSnapshotService;
import org.apache.isis.applib.services.xmlsnapshot.XmlSnapshotServiceAbstract;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

public class XmlSnapshotServiceDefault extends XmlSnapshotServiceAbstract {

    static class XmlSnapshotServiceDefaultBuilder implements XmlSnapshotService.Builder{
        
        private final XmlSnapshotBuilder builder;
        public XmlSnapshotServiceDefaultBuilder(final Object domainObject) {
            builder = new XmlSnapshotBuilder(domainObject);
        }
        
        @Override
        public void includePath(String path) {
            builder.includePath(path);
        }

        @Override
        public void includePathAndAnnotation(String path, String annotation) {
            builder.includePathAndAnnotation(path, annotation);
        }

        public XmlSnapshotService.Snapshot build() {
            XmlSnapshot xmlSnapshot = builder.build();
            return xmlSnapshot;
        }
    } 


    /**
     * Creates a simple snapshot of the domain object.
     */
    public XmlSnapshotService.Snapshot snapshotFor(final Object domainObject) {
        final ObjectAdapter adapter = getAdapterManager().adapterFor(domainObject);
        return new XmlSnapshot(adapter, getOidMarshaller());
    }

    /**
     * Creates a builder that allows a custom snapshot - traversing additional associated
     * properties or collections (using {@link Builder#includePath(String)} and 
     * {@link Builder#includePathAndAnnotation(String, String)}) - to be created.
     */
    public Builder builderFor(final Object domainObject) {
        return new XmlSnapshotServiceDefaultBuilder(domainObject);
    }

    
    // //////////////////////////////////////
    

    protected AdapterManager getAdapterManager() {
        return gerPersistenceSession().getAdapterManager();
    }

    protected PersistenceSession gerPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }


}
