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


package org.apache.isis.runtime.persistence;

import java.util.List;

import org.apache.isis.applib.query.Query;
import org.apache.isis.core.commons.debug.DebugInfo;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.logging.Logger;
import org.apache.isis.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtime.persistence.objectfactory.ObjectFactory;
import org.apache.isis.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.runtime.persistence.query.PersistenceQuery;
import org.apache.isis.runtime.transaction.IsisTransactionManager;


public class PersistenceSessionLogger extends Logger implements PersistenceSession, DebugInfo {
    
    private final PersistenceSession underlying;

    public PersistenceSessionLogger(final PersistenceSession decorated, final String level) {
        super(level);
        this.underlying = decorated;
    }

    public PersistenceSessionLogger(final PersistenceSession decorated) {
        this.underlying = decorated;
    }

    public void destroyObject(final ObjectAdapter object) {
        log("destroy " + object.getObject());
        underlying.destroyObject(object);
    }

    public ObjectAdapter findInstances(Query query, QueryCardinality cardinality) throws UnsupportedFindException {
        log("find instances matching " + query.getDescription());
        return underlying.findInstances(query, cardinality);
    }


	public ObjectAdapter findInstances(PersistenceQuery criteria) {
		log("find instances matching " + criteria.getSpecification());
		return underlying.findInstances(criteria);
	}

    public void debugData(final DebugString debug) {
        underlying.debugData(debug);
    }

    public String debugTitle() {
        return underlying.debugTitle();
    }

    @Override
    protected Class<?> getDecoratedClass() {
        return underlying.getClass();
    }

    public ObjectAdapter loadObject(final Oid oid, final ObjectSpecification hint) throws ObjectNotFoundException {
        final ObjectAdapter object = underlying.loadObject(oid, hint);
        log("get object for " + oid + " (of type " + hint.getShortName() + ")", object.getObject());
        return object;
    }

    public boolean hasInstances(final ObjectSpecification specification) {
        final boolean hasInstances = underlying.hasInstances(specification);
        log("has instances of " + specification.getShortName(), "" + hasInstances);
        return hasInstances;
    }

    public boolean isFixturesInstalled() {
        final boolean isInitialized = underlying.isFixturesInstalled();
        log("is initialized: " + isInitialized);
        return isInitialized;
    }


    public void open() {
        log("opening " + underlying);
        underlying.open();
    }

    public void close() {
        log("closing " + underlying);
        underlying.close();
    }

    public void makePersistent(final ObjectAdapter object) {
        log("make object graph persistent: " + object);
        underlying.makePersistent(object);
    }

    public void objectChanged(final ObjectAdapter object) {
        log("notify of change " + object);
        underlying.objectChanged(object);
    }

    public void reload(final ObjectAdapter object) {
        underlying.reload(object);
        log("reload: " + object);
    }

    public void testReset() {
        log("reset object manager");
        underlying.testReset();
    }

    public void resolveImmediately(final ObjectAdapter object) {
        underlying.resolveImmediately(object);
        log("resolve immediately: " + object);
    }

    public void resolveField(final ObjectAdapter object, final ObjectAssociation field) {
        log("resolve eagerly (object in a field) " + field + " of " + object);
        underlying.resolveField(object, field);
    }

    public void objectChangedAllDirty() {
        log("saving changes");
        underlying.objectChangedAllDirty();
    }


    public ObjectAdapter getService(final String id) {
        log("get service " + id);
        return underlying.getService(id);
    }

    public List<ObjectAdapter> getServices() {
        log("get services ");
        return underlying.getServices();
    }


    public ObjectAdapter createInstance(final ObjectSpecification specification) {
        log("create instance " + specification);
        return underlying.createInstance(specification);
    }

    public ObjectAdapter recreateAdapter(final Oid oid, final ObjectSpecification specification) {
        log("recreate instance " + oid + " " + specification);
        return underlying.recreateAdapter(oid, specification);
    }

    public void setSpecificationLoader(final SpecificationLoader specificationLoader) {
        underlying.setSpecificationLoader(specificationLoader);
    }

    public OidGenerator getOidGenerator() {
        return underlying.getOidGenerator();
    }

    public AdapterFactory getAdapterFactory() {
        return underlying.getAdapterFactory();
    }

    public PersistenceSessionFactory getPersistenceSessionFactory() {
        return underlying.getPersistenceSessionFactory();
    }

    public ServicesInjector getServicesInjector() {
        return underlying.getServicesInjector();
    }

    public IsisTransactionManager getTransactionManager() {
        return underlying.getTransactionManager();
    }

    public void setTransactionManager(IsisTransactionManager transactionManager) {
        underlying.setTransactionManager(transactionManager);
    }

	public ObjectFactory getObjectFactory() {
		return underlying.getObjectFactory();
	}

    public void clearAllDirty() {
        underlying.clearAllDirty();
    }

    public ObjectAdapter reload(Oid oid) {
        return underlying.reload(oid);
    }

    public AdapterManager getAdapterManager() {
        return underlying.getAdapterManager();
    }

    public ObjectAdapter recreateAdapter(Oid oid, Object pojo) {
        return underlying.recreateAdapter(oid, pojo);
    }

    public void injectInto(Object candidate) {
        underlying.injectInto(candidate);
    }


}
