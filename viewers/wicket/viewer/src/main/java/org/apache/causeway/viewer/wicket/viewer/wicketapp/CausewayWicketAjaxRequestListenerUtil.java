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

import org.apache.wicket.SystemMapper;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.core.request.mapper.PageInstanceMapper;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.component.IRequestablePage;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.ui.pages.PageAbstract;

import lombok.val;
import lombok.experimental.UtilityClass;


@UtilityClass
public final class CausewayWicketAjaxRequestListenerUtil {

    public void setRootRequestMapper(
            final WebApplication app,
            final MetaModelContext commonContext) {

        app.setRootRequestMapper(new SystemMapper(app) {
            @Override
            protected IRequestMapper newPageInstanceMapper() {
                return new PageInstanceMapper() {
                    @Override
                    public IRequestHandler mapRequest(final Request request) {
                        var handler = super.mapRequest(request);
                        //final boolean isAjax = ((WebRequest)request).isAjax();

                        if(handler instanceof ListenerRequestHandler) {
//                            _Debug.log("AJAX via ListenerRequestHandler");
//                            RequestCycle.get().getListeners().add(newRequestCycleListener());

                            final IRequestablePage iRequestablePage =
                                    ((ListenerRequestHandler)handler).getPage();

                            if(iRequestablePage instanceof PageAbstract) {
                                val pageAbstract = (PageAbstract) iRequestablePage;
                                pageAbstract.onNewRequestCycle();
                            }

                        }

                        return handler;
                    }
                };
            }
        });
    }

//    public IListener newAjaxListener() {
//
//        RequestCycle x;
//
//        return new IListener() {;
//            @Override
//            public void onBeforeRespond(final Map<String, Component> map, final AjaxRequestTarget target) {
//                _Debug.log("AJAX via IListener");
//                EntityPage.broadcastAjaxRequest(target.getPage(), target);
//
//            }
//        };
//    }

//    private IRequestCycleListener newRequestCycleListener() {
//        return new IRequestCycleListener() {
//            @Override
//            public void onRequestHandlerResolved(final RequestCycle cycle, final IRequestHandler handler) {
//                _Debug.log("RequestCycle: handler resolved %s", handler);
//            }
//        };
//    }

}
