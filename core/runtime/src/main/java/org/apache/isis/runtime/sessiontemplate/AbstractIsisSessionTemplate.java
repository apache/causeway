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
package org.apache.isis.runtime.sessiontemplate;

import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtime.system.session.IsisSession;
import org.apache.isis.runtime.system.session.IsisSessionFactory;
import org.apache.isis.runtime.system.transaction.IsisTransactionManagerJdoInternal;
import org.apache.isis.security.authentication.AuthenticationSession;

public abstract class AbstractIsisSessionTemplate {

    /**
     * Sets up an {@link IsisSession} then passes along any calling framework's context.
     */
    public void execute(final AuthenticationSession authSession, final Object context) {
        try {
            getIsisSessionFactory().openSession(authSession);
            PersistenceSession persistenceSession = getPersistenceSession();
            persistenceSession.getServiceInjector().injectServicesInto(this);
            doExecute(context);
        } finally {
            getIsisSessionFactory().closeSession();
        }
    }

    // //////////////////////////////////////

    /**
     * Either override {@link #doExecute(Object)} (this method) or alternatively override
     * {@link #doExecuteWithTransaction(Object)}.
     *
     * <p>
     * This method is called within a current {@link org.apache.isis.runtime.system.session.IsisSession session},
     * but with no current transaction.  The default implementation sets up a
     * {@link org.apache.isis.runtime.system.transaction.IsisTransaction transaction}
     * and then calls {@link #doExecuteWithTransaction(Object)}.  Override if you require more sophisticated
     * transaction handling.
     */
    protected void doExecute(final Object context) {
        final PersistenceSession persistenceSession = getPersistenceSession();
        final IsisTransactionManagerJdoInternal transactionManager = getTransactionManager(persistenceSession);
        transactionManager.executeWithinTransaction(()->{
            doExecuteWithTransaction(context);
        });
    }

    /**
     * Either override {@link #doExecuteWithTransaction(Object)} (this method) or alternatively override
     * {@link #doExecuteWithTransaction(Object)}.
     *
     * <p>
     * This method is called within a current
     * {@link org.apache.isis.runtime.system.transaction.IsisTransaction transaction}, by the default
     * implementation of {@link #doExecute(Object)}.
     */
    protected void doExecuteWithTransaction(final Object context) {}

    // //////////////////////////////////////

//FIXME[ISIS-1976] not used !?
//    protected final ObjectAdapter adapterFor(final Object targetObject) {
//        if(targetObject instanceof OidDto) {
//            final OidDto oidDto = (OidDto) targetObject;
//            return adapterFor(oidDto);
//        }
//        if(targetObject instanceof CollectionDto) {
//            final CollectionDto collectionDto = (CollectionDto) targetObject;
//            final List<ValueDto> valueDtoList = collectionDto.getValue();
//            final List<Object> pojoList = _Lists.newArrayList();
//            for (final ValueDto valueDto : valueDtoList) {
//                ValueType valueType = collectionDto.getType();
//                final Object valueOrOidDto = CommonDtoUtils.getValue(valueDto, valueType);
//                // converting from adapter and back means we handle both
//                // collections of references and of values
//                final ObjectAdapter objectAdapter = adapterFor(valueOrOidDto);
//                Object pojo = objectAdapter != null ? objectAdapter.getObject() : null;
//                pojoList.add(pojo);
//            }
//            return adapterFor(pojoList);
//        }
//        if(targetObject instanceof Bookmark) {
//            final Bookmark bookmark = (Bookmark) targetObject;
//            return adapterFor(bookmark);
//        }
//        return getPersistenceSession().adapterFor(targetObject);
//    }
//
//    protected final ObjectAdapter adapterFor(final OidDto oidDto) {
//        final Bookmark bookmark = Bookmark.from(oidDto);
//        return adapterFor(bookmark);
//    }
//
//    protected final ObjectAdapter adapterFor(final Bookmark bookmark) {
//        final RootOid rootOid = RootOid.create(bookmark);
//        return adapterFor(rootOid);
//    }
//
//    protected final ObjectAdapter adapterFor(final RootOid rootOid) {
//        return getPersistenceSession().adapterFor(rootOid);
//    }

    // //////////////////////////////////////


    protected IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession().orElse(null);
    }

    protected IsisTransactionManagerJdoInternal getTransactionManager(PersistenceSession persistenceSession) {
        return persistenceSession.getTransactionManager();
    }

    protected SpecificationLoader getSpecificationLoader() {
        return getIsisSessionFactory().getSpecificationLoader();
    }


}
