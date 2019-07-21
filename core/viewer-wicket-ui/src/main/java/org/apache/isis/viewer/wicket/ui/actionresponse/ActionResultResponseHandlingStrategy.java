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
package org.apache.isis.viewer.wicket.ui.actionresponse;

import java.net.URL;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.time.Duration;

import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.models.VoidModel;
import org.apache.isis.viewer.wicket.ui.pages.voidreturn.VoidReturnPage;

public enum ActionResultResponseHandlingStrategy {
    REDIRECT_TO_VOID {
        @Override
        public void handleResults(
                ActionResultResponse resultResponse,
                final IsisSessionFactory isisSessionFactory) {
            final RequestCycle requestCycle = RequestCycle.get();
            requestCycle.setResponsePage(new VoidReturnPage(new VoidModel()));
        }
    },
    REDIRECT_TO_PAGE {
        @Override
        public void handleResults(
                final ActionResultResponse resultResponse,
                final IsisSessionFactory isisSessionFactory) {
            // force any changes in state etc to happen now prior to the redirect;
            // in the case of an object being returned, this should cause our page mementos
            // (eg EntityModel) to hold the correct state.  I hope.

        	IsisContext.getTransactionService().flushTransaction();

            // "redirect-after-post"
            final RequestCycle requestCycle = RequestCycle.get();
            requestCycle.setResponsePage(resultResponse.getToPage());
        }
    },
    SCHEDULE_HANDLER {
        @Override
        public void handleResults(
                final ActionResultResponse resultResponse,
                final IsisSessionFactory isisSessionFactory) {
            final RequestCycle requestCycle = RequestCycle.get();
            AjaxRequestTarget target = requestCycle.find(AjaxRequestTarget.class).orElse(null);

            if (target == null) {
                // non-Ajax request => just stream the Lob to the browser
                // or if this is a no-arg action, there also will be no parent for the component
                requestCycle.scheduleRequestHandlerAfterCurrent(resultResponse.getHandler());
            } else {
                // otherwise,
                // Ajax request => respond with a redirect to be able to stream the Lob to the client
                ResourceStreamRequestHandler scheduledHandler = (ResourceStreamRequestHandler) resultResponse.getHandler();
                StreamAfterAjaxResponseBehavior streamingBehavior = new StreamAfterAjaxResponseBehavior(scheduledHandler);
                final Page page = target.getPage();
                page.add(streamingBehavior);
                CharSequence callbackUrl = streamingBehavior.getCallbackUrl();
                target.appendJavaScript("setTimeout(\"window.location.href='" + callbackUrl + "'\", 10);");
            }

        }
    },
    OPEN_URL_IN_BROWSER {
        @Override
        public void handleResults(
                final ActionResultResponse resultResponse,
                final IsisSessionFactory isisSessionFactory) {
            final AjaxRequestTarget target = resultResponse.getTarget();
            final URL url = resultResponse.getUrl();

            final RequestCycle requestCycle = RequestCycle.get();

            final String fullUrl = expanded(requestCycle, url);
            target.appendJavaScript("setTimeout(function(){Wicket.Event.publish(Isis.Topic.OPEN_IN_NEW_TAB, '" + fullUrl + "');}, 100);");
        }

    };

    public abstract void handleResults(
            ActionResultResponse resultResponse,
            final IsisSessionFactory isisSessionFactory);

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

    /**
     * A special Ajax behavior that is used to stream the contents of a Lob after
     * an Ajax request.
     */
    private static class StreamAfterAjaxResponseBehavior extends AbstractAjaxBehavior {
        private static final long serialVersionUID = 1L;

        private final String fileName;
        private final IResourceStream resourceStream;
        private final Duration cacheDuration;

        public StreamAfterAjaxResponseBehavior(ResourceStreamRequestHandler scheduledHandler) {
            this.fileName = scheduledHandler.getFileName();
            this.resourceStream = scheduledHandler.getResourceStream();
            this.cacheDuration = scheduledHandler.getCacheDuration();
        }

        @Override
        public void onRequest() {
            ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(resourceStream, fileName);
            handler.setCacheDuration(cacheDuration);
            handler.setContentDisposition(ContentDisposition.ATTACHMENT);
            Component page = getComponent();
            page.getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
            page.remove(this);
        }
    }
}
