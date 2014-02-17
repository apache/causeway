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
package org.apache.isis.viewer.wicket.ui.actionresponse;

import org.apache.wicket.Component;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.isis.core.runtime.system.context.IsisContext;

public enum ActionResultResponseHandlingStrategy {
    REDIRECT_TO_PAGE {
        @Override
        public void handleResults(final Component component, final ActionResultResponse resultResponse) {
            // force any changes in state etc to happen now prior to the redirect;
            // in the case of an object being returned, this should cause our page mementos 
            // (eg EntityModel) to hold the correct state.  I hope.
            IsisContext.getTransactionManager().flushTransaction();
            
            // "redirect-after-post"
            component.setResponsePage(resultResponse.getToPage());
        }
    },
    SCHEDULE_HANDLER {
        @Override
        public void handleResults(final Component component, final ActionResultResponse resultResponse) {
            RequestCycle requestCycle = component.getRequestCycle();
            requestCycle.scheduleRequestHandlerAfterCurrent(resultResponse.getHandler());
        }
    };

    public abstract void handleResults(Component component, ActionResultResponse resultResponse);

    public static ActionResultResponseHandlingStrategy determineFor(final ActionResultResponse resultResponse) {
        if(resultResponse.isToPage()) {
            return REDIRECT_TO_PAGE;
        } else {
            return SCHEDULE_HANDLER;
        }
    }
}