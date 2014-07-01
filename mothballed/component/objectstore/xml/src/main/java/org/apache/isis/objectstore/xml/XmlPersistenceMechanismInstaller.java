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

package org.apache.isis.objectstore.xml;

import java.util.Date;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstallerAbstract;
import org.apache.isis.core.runtime.persistence.objectstore.ObjectStoreSpi;
import org.apache.isis.core.runtime.system.persistence.AdapterManagerSpi;
import org.apache.isis.core.runtime.system.persistence.IdentifierGenerator;
import org.apache.isis.core.runtime.system.persistence.IdentifierGeneratorDefault;
import org.apache.isis.objectstore.xml.internal.clock.DefaultClock;

public class XmlPersistenceMechanismInstaller extends PersistenceMechanismInstallerAbstract {

    public static final String NAME = "xml";
    
    private XmlObjectStore objectStore;

    public XmlPersistenceMechanismInstaller() {
        super(NAME);
    }

    @Override
    protected ObjectStoreSpi createObjectStore(final IsisConfiguration configuration, final ObjectAdapterFactory objectFactory, final AdapterManagerSpi adapterManager) {
        if (objectStore == null) {
            objectStore = new XmlObjectStore(configuration);
            objectStore.setClock(new DefaultClock());
        }
        return objectStore;
    }

    @Override
    public IdentifierGenerator createIdentifierGenerator(final IsisConfiguration configuration) {
        final long currentTime = new Date().getTime();
        return new IdentifierGeneratorDefault(currentTime);
    }
}
