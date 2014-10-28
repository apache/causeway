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

import java.net.URL;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.models.VoidModel;
import org.apache.isis.viewer.wicket.ui.pages.voidreturn.VoidReturnPage;

public enum ActionResultResponseHandlingStrategy {
    REDIRECT_TO_VOID {
        @Override
        public void handleResults(Component component, ActionResultResponse resultResponse) {
            component.setResponsePage(new VoidReturnPage(new VoidModel()));
        }
    },
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
    },
    OPEN_URL_IN_BROWSER {
        @Override
        public void handleResults(final Component component, final ActionResultResponse resultResponse) {
            final AjaxRequestTarget target = resultResponse.getTarget();
            final URL url = resultResponse.getUrl();
            
            RequestCycle requestCycle = component.getRequestCycle();

            final String fullUrl = expanded(requestCycle, url);
            target.appendJavaScript("setTimeout(function(){Wicket.Event.publish(Isis.Topic.OPEN_IN_NEW_TAB, '" + fullUrl + "');}, 100);");
        }

    };

    public abstract void handleResults(Component component, ActionResultResponse resultResponse);

    /**
     * @see #expanded(String)
     */
    public static String expanded(RequestCycle requestCycle, final URL url) {
        String urlStr = expanded(url);
        return requestCycle.getUrlRenderer().renderFullUrl(Url.parse(urlStr));
    }

    /**
     * @see #expanded(String)
     */
    public static String expanded(final URL url) {
        return expanded(url.toString());
    }

    /**
     * very simple templating support, the idea being that "antiCache=${currentTimeMillis}"
     * will be replaced automatically.
     */
    public static String expanded(String urlStr) {
        if(urlStr.contains("antiCache=${currentTimeMillis}")) {
            urlStr = urlStr.replace("antiCache=${currentTimeMillis}", "antiCache="+System.currentTimeMillis());
        }
        return urlStr;
    }

}
