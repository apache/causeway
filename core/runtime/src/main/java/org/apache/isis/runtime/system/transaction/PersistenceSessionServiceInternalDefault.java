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
package org.apache.isis.runtime.system.transaction;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.Oid.Factory;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtime.system.session.IsisSessionFactory;

import static org.apache.isis.commons.internal.base._With.acceptIfPresent;
import static org.apache.isis.commons.internal.base._With.mapIfPresentElse;

@Service
public class PersistenceSessionServiceInternalDefault 
implements PersistenceSessionServiceInternal {

    @Override
    public ObjectAdapterProvider getObjectAdapterProvider() {
        return getPersistenceSession();
    }

    @Override
    public void makePersistent(final ObjectAdapter adapter) {
        getPersistenceSession().makePersistentInTransaction(adapter);
    }

    @Override
    public void remove(final ObjectAdapter adapter) {
        getPersistenceSession().destroyObjectInTransaction(adapter);
    }

    @Override
    public Object lookup(
            final Bookmark bookmark,
            final BookmarkService.FieldResetPolicy fieldResetPolicy) {

        final RootOid rootOid = Factory.ofBookmark(bookmark);
        final PersistenceSession ps = getPersistenceSession();
        final boolean denyRefresh = fieldResetPolicy == BookmarkService.FieldResetPolicy.DONT_REFRESH; 

        if(rootOid.isViewModel()) {
            final ObjectAdapter adapter = ps.adapterFor(rootOid);
            final Object pojo = mapIfPresentElse(adapter, ObjectAdapter::getPojo, null);

            return pojo;

        } else if(denyRefresh) {

            final Object pojo = ps.fetchPersistentPojoInTransaction(rootOid);
            return pojo;            

        } else {
            final ObjectAdapter adapter = ps.adapterFor(rootOid);

            final Object pojo = mapIfPresentElse(adapter, ObjectAdapter::getPojo, null);
            acceptIfPresent(pojo, ps::refreshRootInTransaction);
            return pojo;
        }

    }

    @Override
    public Bookmark bookmarkFor(Object domainObject) {
        final ObjectAdapter adapter = getPersistenceSession().adapterFor(domainObject);
        if(adapter.isValue()) {
            // values cannot be bookmarked
            return null;
        }
        final Oid oid = adapter.getOid();
        if(!(oid instanceof RootOid)) {
            // must be root
            return null;
        }
        final RootOid rootOid = (RootOid) oid;
        return rootOid.asBookmark();
    }

    @Override
    public Bookmark bookmarkFor(Class<?> cls, String identifier) {
        final ObjectSpecification objectSpec = specificationLoader.loadSpecification(cls);
        String objectType = objectSpec.getSpecId().asString();
        return new Bookmark(objectType, identifier);
    }

    @Override
    public void resolve(final Object parent) {
        getPersistenceSession().refreshRootInTransaction(parent);
    }

    @Override
    public <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query) {
        return getPersistenceSession().allMatchingQuery(query);
    }

    @Override
    public <T> ObjectAdapter firstMatchingQuery(final Query<T> query) {
        return getPersistenceSession().firstMatchingQuery(query);
    }

    protected PersistenceSession getPersistenceSession() {
        return PersistenceSession.current(PersistenceSession.class)
                .getFirst()
                .orElseThrow(()->new NonRecoverableException("No IsisSession on current thread."));
    }

    @Inject IsisSessionFactory isisSessionFactory;
    @Inject SpecificationLoader specificationLoader;

}
