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

package org.apache.isis.runtimes.dflt.runtime.services;

import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.Persistor;

public final class SimpleRepository {

    private final Class<?> type;
    private ObjectSpecification spec;

    public SimpleRepository(final Class<?> cls) {
        this.type = cls;
    }

    public String getId() {
        return "repository#" + getClass().getName();
    }

    public String iconName() {
        return getSpec().getShortIdentifier();
    }

    public String title() {
        return getSpec().getPluralName();
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("class", type.getName());
        return str.toString();
    }

    // //////////////////////////////////////////////////////
    // allInstances
    // //////////////////////////////////////////////////////

    public Object[] allInstances() {
        return RepositoryHelper.allInstances(getSpec(), type);
    }

    public String disableAllInstances() {
        return hasInstances() ? null : "No " + getSpec().getPluralName();
    }

    // //////////////////////////////////////////////////////
    // allInstances
    // //////////////////////////////////////////////////////

    public Object[] findByTitle(final String title) {
        return RepositoryHelper.findByTitle(getSpec(), type, title).toArray();
    }

    public String disableFindByTitle() {
        return disableAllInstances();
    }

    public static String[] parameterNamesFindByTitle() {
        return new String[] { "Title to find" };
    }

    public static boolean[] parametersRequiredFindByTitle() {
        return new boolean[] { true };
    }

    private boolean hasInstances() {
        return RepositoryHelper.hasInstances(getSpec());
    }

    // //////////////////////////////////////////////////////
    // newPersistentInstance
    // //////////////////////////////////////////////////////

    public Object newPersistentInstance() {
        final ObjectAdapter adapter = getPersistenceSession().createInstance(getSpec());
        getPersistenceSession().makePersistent(adapter);
        return adapter.getObject();
    }

    // //////////////////////////////////////////////////////
    // newTransientInstance
    // //////////////////////////////////////////////////////

    public Object newTransientInstance() {
        return getPersistenceSession().createInstance(getSpec()).getObject();
    }

    // //////////////////////////////////////////////////////
    // getSpec (hidden)
    // //////////////////////////////////////////////////////

    protected ObjectSpecification getSpec() {
        if (spec == null) {
            spec = IsisContext.getSpecificationLoader().loadSpecification(type);
        }
        return spec;
    }

    public static boolean alwaysHideSpec() {
        return true;
    }

    // //////////////////////////////////////////////////////
    // Dependencies (from singleton)
    // //////////////////////////////////////////////////////

    private Persistor getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}
