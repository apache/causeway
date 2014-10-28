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

package org.apache.isis.core.objectstore;

import java.lang.reflect.Modifier;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.spec.ObjectInstantiationException;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.runtime.bytecode.dflt.ClassSubstitutorDefault;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstallerAbstract;
import org.apache.isis.core.runtime.persistence.objectfactory.ObjectFactoryAbstract;
import org.apache.isis.core.runtime.persistence.objectstore.ObjectStoreSpi;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.persistence.AdapterManagerSpi;
import org.apache.isis.core.runtime.system.persistence.ObjectFactory;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;

/**
 * Installs the in-memory object store.
 */
public class InMemoryPersistenceMechanismInstaller extends PersistenceMechanismInstallerAbstract {

    public static final String NAME = "in-memory";

    public InMemoryPersistenceMechanismInstaller() {
        super(NAME);
    }

    // ///////////////////////////////////////////////////////////////
    // createPersistenceSessionFactory
    // ///////////////////////////////////////////////////////////////

    @Override
    public PersistenceSessionFactory createPersistenceSessionFactory(final DeploymentType deploymentType) {
        return new InMemoryPersistenceSessionFactory(deploymentType, getConfiguration(), this);
    }

    // ///////////////////////////////////////////////////////////////
    // Hook methods
    // ///////////////////////////////////////////////////////////////

    /**
     * Hook method to return {@link ObjectStoreSpi}.
     */
    @Override
    protected ObjectStoreSpi createObjectStore(final IsisConfiguration configuration, final ObjectAdapterFactory adapterFactory, final AdapterManagerSpi adapterManager) {
        return new InMemoryObjectStore();
    }

    
    @Override
    public ObjectFactory createObjectFactory(IsisConfiguration configuration) {
        return new ObjectFactoryBasic();
    }

    @Override
    public ClassSubstitutor createClassSubstitutor(IsisConfiguration configuration) {
        return new ClassSubstitutorDefault();
    }


}


class ObjectFactoryBasic extends ObjectFactoryAbstract {

    public ObjectFactoryBasic() {
    }

    public ObjectFactoryBasic(final Mode mode) {
        super(mode);
    }

    /**
     * Simply instantiates reflectively, does not enhance bytecode etc in any
     * way.
     */
    @Override
    protected <T> T doInstantiate(final Class<T> cls) throws ObjectInstantiationException {
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new ObjectInstantiationException("Cannot create an instance of an abstract class: " + cls);
        }
        try {
            return cls.newInstance();
        } catch (final IllegalAccessException e) {
            throw new ObjectInstantiationException(e);
        } catch (final InstantiationException e) {
            throw new ObjectInstantiationException(e);
        }
    }

}
