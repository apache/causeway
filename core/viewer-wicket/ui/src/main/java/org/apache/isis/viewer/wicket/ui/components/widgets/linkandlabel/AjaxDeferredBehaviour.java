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

package org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseHandlingStrategy;

public abstract class AjaxDeferredBehaviour extends AbstractAjaxBehavior {
    private static final long serialVersionUID = 1L;

    public static enum OpenUrlStrategy {
        NEW_WINDOW {
            @Override
            public String javascriptFor(AjaxDeferredBehaviour deferredBehaviour, String url) {
                final Url parsedUrl = Url.parse(url);
                final RequestCycle requestCycle = deferredBehaviour.getComponent().getRequestCycle();
                final UrlRenderer urlRenderer = requestCycle.getUrlRenderer();
                String fullUrl = urlRenderer.renderFullUrl(parsedUrl);
                return "function(){Wicket.Event.publish(Isis.Topic.OPEN_IN_NEW_TAB, '" + fullUrl + "');}";
            }
        },
        SAME_WINDOW {
            @Override
            public String javascriptFor(AjaxDeferredBehaviour deferredBehaviour, String url) {
                return "\"window.location.href='" + url + "'\"";
            }
        };

        public abstract String javascriptFor(AjaxDeferredBehaviour deferredBehaviour, String url);
    }

    private final OpenUrlStrategy openUrlStrategy;

    protected AjaxDeferredBehaviour(final OpenUrlStrategy openUrlStrategy) {
        this.openUrlStrategy = openUrlStrategy;
    }

    /**
     * Call this method to initiate the download.
     */
    public void initiate(AjaxRequestTarget target) {
        String url = getCallbackUrl().toString();

        url = ActionResultResponseHandlingStrategy.expanded(url);
        String func = openUrlStrategy.javascriptFor(this, url);

        // the timeout is needed to let Wicket release the channel
        String javascriptFor = "setTimeout(" + func + ", 100);";
        target.appendJavaScript(javascriptFor);
    }

    @Override
    public void onRequest() {
        IRequestHandler handler = getRequestHandler();
        if(handler != null) {
            getComponent().getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
        }
    }

    protected abstract IRequestHandler getRequestHandler();
}
