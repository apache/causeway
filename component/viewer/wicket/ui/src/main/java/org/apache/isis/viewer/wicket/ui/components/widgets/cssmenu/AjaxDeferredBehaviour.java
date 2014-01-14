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

package org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.IResourceStream;

public abstract class AjaxDeferredBehaviour extends AbstractAjaxBehavior {
    private static final long serialVersionUID = 1L;
    private boolean addAntiCache;

    public AjaxDeferredBehaviour() {
        this(true);
    }

    public AjaxDeferredBehaviour(boolean addAntiCache) {
        this.addAntiCache = addAntiCache;
    }

    /**
     * Call this method to initiate the download.
     */
    public void initiate(AjaxRequestTarget target) {
        String url = getCallbackUrl().toString();

        if (addAntiCache) {
            url = url + (url.contains("?") ? "&" : "?");
            url = url + "antiCache=" + System.currentTimeMillis();
        }

        // the timeout is needed to let Wicket release the channel
        String javascriptFor = javascriptFor(url);
        target.appendJavaScript(javascriptFor);
    }

    protected String javascriptFor(String url) {
        return "setTimeout(\"window.location.href='" + url + "'\", 100);";
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