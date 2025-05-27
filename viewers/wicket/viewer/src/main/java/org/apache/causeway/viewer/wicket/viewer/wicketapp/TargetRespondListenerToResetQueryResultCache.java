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
package org.apache.causeway.viewer.wicket.viewer.wicketapp;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class TargetRespondListenerToResetQueryResultCache implements AjaxRequestTarget.ITargetRespondListener {

    @Inject private Provider<QueryResultsCache> queryResultsCacheProvider;

    @Override
    public void onTargetRespond(final AjaxRequestTarget target) {

        if(log.isDebugEnabled()) {
            log.debug("RESPOND PHASE STARTED: resetting cache");
        }

        queryResultsCacheProvider.get().onTransactionEnded();
    }

}
