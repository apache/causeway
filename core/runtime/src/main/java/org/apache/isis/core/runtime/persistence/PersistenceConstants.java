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

package org.apache.isis.core.runtime.persistence;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.services.container.DomainObjectContainerDefault;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.runtime.system.persistence.ObjectFactory;

public final class PersistenceConstants {


    public static final String ENFORCE_SAFE_SEMANTICS = "isis.persistor.enforceSafeSemantics";
    /**
     * Default is <code>false</code> only for backward compatibility (to avoid lots of breakages in existing code);
     * in future might change to <code>true</code>.
     */
    public static final boolean ENFORCE_SAFE_SEMANTICS_DEFAULT = false;

    /**
     * Default implementation to use as {@link ObjectFactory}.
     */
    public static final String OBJECT_FACTORY_CLASS_NAME_DEFAULT = "org.apache.isis.core.bytecode.cglib.CglibObjectFactory";
    
    /**
     * Default implementation to use as {@link ClassSubstitutor}.
     */
    public static final String CLASS_SUBSTITUTOR_CLASS_NAME_DEFAULT = "org.apache.isis.core.bytecode.cglib.CglibClassSubstitutor";

    /**
     * Key used to lookup implementation of {@link DomainObjectContainer} in
     * {@link IsisConfiguration}.
     */
    public static final String DOMAIN_OBJECT_CONTAINER_CLASS_NAME = ConfigurationConstants.ROOT + "persistor.domain-object-container";
    public static final String DOMAIN_OBJECT_CONTAINER_NAME_DEFAULT = DomainObjectContainerDefault.class.getName();

    private PersistenceConstants() {
    }

}
