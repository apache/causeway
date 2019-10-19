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
package org.apache.isis.viewer.wicket.viewer;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.tracing.TraceScopeManager;

class TargetRespondListenerToResetQueryResultCache implements AjaxRequestTarget.ITargetRespondListener {

    private static final Logger LOG = LoggerFactory.getLogger(TargetRespondListenerToResetQueryResultCache.class);

    @Override
    public void onTargetRespond(final AjaxRequestTarget target) {

        TraceScopeManager.get()
                .activeSpan()
                .log("onTargetResponse : RESPOND PHASE STARTED, resetting cache")
                ;

        if(LOG.isDebugEnabled()) {
            LOG.debug("RESPOND PHASE STARTED: resetting cache");
        }

        final QueryResultsCache queryResultsCache = lookupQueryResultsCache();
        queryResultsCache.resetForNextTransaction();
    }

    private QueryResultsCache lookupQueryResultsCache() {
        return getServicesInjector().lookupService(QueryResultsCache.class);
    }

    private ServicesInjector getServicesInjector() {
        return getSessionFactory().getServicesInjector();
    }

    private IsisSessionFactory getSessionFactory() {
        return IsisContext.getSessionFactory();
    }
}
