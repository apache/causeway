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
package org.apache.isis.viewer.bdd.common.fixtures;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.ObjectStore;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;

public abstract class AbstractFixturePeer {

    private final AliasRegistry aliasRegistry;
    private final List<CellBinding> cellBindings;

    public AbstractFixturePeer(final AliasRegistry aliasRegistry, final CellBinding... cellBindings) {
        this(aliasRegistry, Arrays.asList(cellBindings));
    }

    public AbstractFixturePeer(final AliasRegistry storyRegistries, final List<CellBinding> cellBindings) {
        this.aliasRegistry = storyRegistries;
        this.cellBindings = cellBindings;
    }

    public AliasRegistry getAliasRegistry() {
        return aliasRegistry;
    }

    public List<CellBinding> getCellBindings() {
        return cellBindings;
    }

    public List<Object> getServices() {
        return IsisContext.getServices();
    }

    public SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    public AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

    public PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected ObjectStore getObjectStore() {
        return getPersistenceSession().getObjectStore();
    }

    protected IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }

    public boolean isValidAlias(final String alias) {
        return getAliasRegistry().getAliased(alias) != null;
    }

}
