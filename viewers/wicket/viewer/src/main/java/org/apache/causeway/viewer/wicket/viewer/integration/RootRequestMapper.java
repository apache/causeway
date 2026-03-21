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
package org.apache.causeway.viewer.wicket.viewer.integration;

import org.apache.wicket.Application;
import org.apache.wicket.SystemMapper;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.core.request.mapper.PageInstanceMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;

import org.apache.causeway.viewer.wicket.ui.pages.PageAbstract;

@Deprecated // was replaced by RehydrationHandler
public final class RootRequestMapper extends SystemMapper implements IRequestMapper {

    @Deprecated
    public RootRequestMapper(final Application application) {
        super(application);
    }

    // intercept AJAX requests and reload view-models so any detached entities are re-fetched
    @Deprecated
    @Override
    protected IRequestMapper newPageInstanceMapper() {
        return new PageInstanceMapper() {
            @Override
            public IRequestHandler mapRequest(final Request request) {
                var handler = super.mapRequest(request);
                if (handler instanceof ListenerRequestHandler listenerRequestHandler
                        && listenerRequestHandler.getPage() instanceof PageAbstract pageAbstract) {
                    pageAbstract.onNewRequestCycle();
                }
                return handler;
            }
        };
    }

}
