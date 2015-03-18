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

package org.apache.isis.core.runtime.transaction.facets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.DecoratingFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosureWithReturnAbstract;

public class ActionInvocationFacetWrapTransaction extends ActionInvocationFacetAbstract implements DecoratingFacet<ActionInvocationFacet> {

    private final static Logger LOG = LoggerFactory.getLogger(ActionInvocationFacetWrapTransaction.class);

    private final ActionInvocationFacet underlyingFacet;

    @Override
    public ActionInvocationFacet getDecoratedFacet() {
        return underlyingFacet;
    }

    public ActionInvocationFacetWrapTransaction(final ActionInvocationFacet underlyingFacet) {
        super(underlyingFacet.getFacetHolder());
        this.underlyingFacet = underlyingFacet;
    }

    @Override
    public ObjectAdapter invoke(final ObjectAction owningAction, final ObjectAdapter targetAdapter, final ObjectAdapter[] argumentAdapters) {
        final ObjectAdapter result = getTransactionManager().executeWithinTransaction(new TransactionalClosureWithReturnAbstract<ObjectAdapter>() {
            @Override
            public ObjectAdapter execute() {
                return underlyingFacet.invoke(owningAction, targetAdapter, argumentAdapters);
            }
        });
        return result;
    }

    @Override
    public ObjectAdapter invoke(final ObjectAdapter targetAdapter, final ObjectAdapter[] argumentAdapters) {
        final ObjectAdapter result = getTransactionManager().executeWithinTransaction(new TransactionalClosureWithReturnAbstract<ObjectAdapter>() {
            @Override
            public ObjectAdapter execute() {
                return underlyingFacet.invoke(targetAdapter, argumentAdapters);
            }
        });
        return result;
    }

    @Override
    public ObjectSpecification getReturnType() {
        return underlyingFacet.getReturnType();
    }

    @Override
    public ObjectSpecification getOnType() {
        return underlyingFacet.getOnType();
    }

    @Override
    public String toString() {
        return super.toString() + " --> " + underlyingFacet.toString();
    }

    // ///////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////////////////////////

    private static IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }


}
