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
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.component.IRequestablePage;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.ui.pages.PageAbstract;

import lombok.extern.slf4j.Slf4j;

public final class RootRequestMapper extends SystemMapper implements IRequestMapper {

    public static ThreadLocal<InteractionContext> X = new ThreadLocal<>();

    @Slf4j
    record RequestHandlerWrapper(
            InteractionService interactionService,
            InteractionContext interactionContext,
            _Lazy<IRequestHandler> delegate) implements IRequestHandler {

        @Override
        public void respond(final IRequestCycle requestCycle) {
            if(interactionContext==null) {
                delegate.get().respond(requestCycle);
                return;
            }
            interactionService.testSupport().openInteraction(interactionContext);
            X.remove();
            delegate.get().respond(requestCycle);


//            interactionService.run(interactionContext, ()->{
//                delegate.get().respond(requestCycle);
//                X.remove();
//            });
        }
        @Override
        public void detach(final IRequestCycle requestCycle) {
            if(delegate.isMemoized()) {
                delegate.get().detach(requestCycle);
            }
            interactionService.closeInteractionLayers();
        }
    }

    public RootRequestMapper(final Application application) {
        super(application);
    }

    @Override
    public IRequestHandler mapRequest(final Request request) {
        var mmc = MetaModelContext.instanceElseFail();
//        var ic = new SessionAuthenticator(mmc.getInteractionService(), mmc.lookupServiceElseFail(UserService.class))
//            .determineInteractionContext()
//            .orElse(null);

        return new RequestHandlerWrapper(
                        mmc.getInteractionService(),
                        X.get(),
                        _Lazy.threadSafe(()->super.mapRequest(request)));
    }

    // intercept AJAX requests and reload view-models so any detached entities are re-fetched
    @Override
    protected IRequestMapper newPageInstanceMapper() {
        return new PageInstanceMapper() {
            @Override
            public IRequestHandler mapRequest(final Request request) {
                var handler = super.mapRequest(request);

                if (handler instanceof ListenerRequestHandler) {

                    final IRequestablePage iRequestablePage = ((ListenerRequestHandler) handler).getPage();

                    if (iRequestablePage instanceof PageAbstract pageAbstract) {
                        pageAbstract.onNewRequestCycle();
                    }
                }

                return handler;
            }
        };
    }

}
