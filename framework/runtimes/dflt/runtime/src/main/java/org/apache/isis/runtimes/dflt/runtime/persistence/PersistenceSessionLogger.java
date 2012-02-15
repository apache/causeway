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

package org.apache.isis.runtimes.dflt.runtime.persistence;

import java.util.List;

import org.apache.isis.applib.query.Query;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.stringable.directly.OidWithSpecification;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.logging.Logger;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.ObjectFactory;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;

public class PersistenceSessionLogger extends Logger implements PersistenceSession, DebuggableWithTitle {

    private final PersistenceSession underlying;

    public PersistenceSessionLogger(final PersistenceSession decorated, final String level) {
        super(level);
        this.underlying = decorated;
    }

    public PersistenceSessionLogger(final PersistenceSession decorated) {
        this.underlying = decorated;
    }

    @Override
    public void destroyObject(final ObjectAdapter object) {
        log("destroy " + object.getObject());
        underlying.destroyObject(object);
    }

    @Override
    public ObjectAdapter findInstances(final Query query, final QueryCardinality cardinality) throws UnsupportedFindException {
        log("find instances matching " + query.getDescription());
        return underlying.findInstances(query, cardinality);
    }

    @Override
    public ObjectAdapter findInstances(final PersistenceQuery criteria) {
        log("find instances matching " + criteria.getSpecification());
        return underlying.findInstances(criteria);
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        underlying.debugData(debug);
    }

    @Override
    public String debugTitle() {
        return underlying.debugTitle();
    }

    @Override
    protected Class<?> getDecoratedClass() {
        return underlying.getClass();
    }

    @Override
    public ObjectAdapter loadObject(final Oid oid, final ObjectSpecification hint) throws ObjectNotFoundException {
        final ObjectAdapter object = underlying.loadObject(oid, hint);
        log("get object for " + oid + " (of type " + hint.getShortIdentifier() + ")", object.getObject());
        return object;
    }

    @Override
    public boolean hasInstances(final ObjectSpecification specification) {
        final boolean hasInstances = underlying.hasInstances(specification);
        log("has instances of " + specification.getShortIdentifier(), "" + hasInstances);
        return hasInstances;
    }

    @Override
    public boolean isFixturesInstalled() {
        final boolean isInitialized = underlying.isFixturesInstalled();
        log("is initialized: " + isInitialized);
        return isInitialized;
    }

    @Override
    public void open() {
        log("opening " + underlying);
        underlying.open();
    }

    @Override
    public void close() {
        log("closing " + underlying);
        underlying.close();
    }

    @Override
    public void makePersistent(final ObjectAdapter object) {
        log("make object graph persistent: " + object);
        underlying.makePersistent(object);
    }

    @Override
    public void objectChanged(final ObjectAdapter object) {
        log("notify of change " + object);
        underlying.objectChanged(object);
    }

    @Override
    public void reload(final ObjectAdapter object) {
        underlying.reload(object);
        log("reload: " + object);
    }

    @Override
    public void testReset() {
        log("reset object manager");
        underlying.testReset();
    }

    @Override
    public void resolveImmediately(final ObjectAdapter object) {
        underlying.resolveImmediately(object);
        log("resolve immediately: " + object);
    }

    @Override
    public void resolveField(final ObjectAdapter object, final ObjectAssociation field) {
        log("resolve eagerly (object in a field) " + field + " of " + object);
        underlying.resolveField(object, field);
    }

    @Override
    public void objectChangedAllDirty() {
        log("saving changes");
        underlying.objectChangedAllDirty();
    }

    @Override
    public ObjectAdapter getService(final String id) {
        log("get service " + id);
        return underlying.getService(id);
    }

    @Override
    public List<ObjectAdapter> getServices() {
        log("get services ");
        return underlying.getServices();
    }

    @Override
    public ObjectAdapter createInstance(final ObjectSpecification specification) {
        log("create instance " + specification);
        return underlying.createInstance(specification);
    }

    @Override
    public ObjectAdapter createAggregatedInstance(final ObjectSpecification specification, final ObjectAdapter parent) {
        log("create aggregated instance " + specification + " as part of " + parent);
        return underlying.createAggregatedInstance(specification, parent);
    }

    @Override
    public ObjectAdapter recreateAdapter(OidWithSpecification oid) {
        log("recreate instance " + oid);
        return underlying.recreateAdapter(oid);
    }

    @Override
    public ObjectAdapter recreateAdapter(final Oid oid, final ObjectSpecification specification) {
        log("recreate instance " + oid + " " + specification);
        return underlying.recreateAdapter(oid, specification);
    }

    @Override
    public void setSpecificationLoader(final SpecificationLoader specificationLoader) {
        underlying.setSpecificationLoader(specificationLoader);
    }

    @Override
    public OidGenerator getOidGenerator() {
        return underlying.getOidGenerator();
    }

    @Override
    public ObjectAdapterFactory getAdapterFactory() {
        return underlying.getAdapterFactory();
    }

    @Override
    public PersistenceSessionFactory getPersistenceSessionFactory() {
        return underlying.getPersistenceSessionFactory();
    }

    @Override
    public ServicesInjector getServicesInjector() {
        return underlying.getServicesInjector();
    }

    @Override
    public IsisTransactionManager getTransactionManager() {
        return underlying.getTransactionManager();
    }

    @Override
    public void setTransactionManager(final IsisTransactionManager transactionManager) {
        underlying.setTransactionManager(transactionManager);
    }

    @Override
    public ObjectFactory getObjectFactory() {
        return underlying.getObjectFactory();
    }

    @Override
    public void clearAllDirty() {
        underlying.clearAllDirty();
    }

    @Override
    public ObjectAdapter reload(final Oid oid) {
        return underlying.reload(oid);
    }

    @Override
    public AdapterManager getAdapterManager() {
        return underlying.getAdapterManager();
    }

    @Override
    public ObjectAdapter recreateAdapter(final Oid oid, final Object pojo) {
        return underlying.recreateAdapter(oid, pojo);
    }

    @Override
    public void injectInto(final Object candidate) {
        underlying.injectInto(candidate);
    }


}
