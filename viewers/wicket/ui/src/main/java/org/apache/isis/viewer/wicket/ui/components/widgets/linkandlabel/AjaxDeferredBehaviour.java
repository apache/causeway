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

import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseHandlingStrategy;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public abstract class AjaxDeferredBehaviour extends AbstractAjaxBehavior {
    private static final long serialVersionUID = 1L;
    
    private final @NonNull ActionModel actionModel;
    
    // -- FACTORIES

    public static AjaxDeferredBehaviour redirecting(final @NonNull ActionModel actionModel){
        
        /**
         * adapted from:
         *
         * @see https://cwiki.apache.org/confluence/display/WICKET/AJAX+update+and+file+download+in+one+blow
         */
        return new AjaxDeferredBehaviour(actionModel) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean needsDeferring() {
                // introspect the action result, to decide whether needs deferring
                val value = getResultValue();
                if(value==null) {
                    return false;
                }
                return value instanceof LocalResourcePath
                        || value instanceof java.net.URL;
            }
            
            
            @Override
            protected String javascriptFor(String url) {
                // introspect the action result, to decide which JS to use
                val value = getResultValue();
                if(value instanceof LocalResourcePath) {
                    if(((LocalResourcePath)value).getOpenUrlStrategy().isSameWindow()) {
                        return javascriptFor_sameWindow(this, url);
                    }
                }
                return javascriptFor_newWindow(this, url);
            }
            
            @Override
            protected IRequestHandler getRequestHandler() {
                val value = getResultValue();
                freeResultValue();
                return ActionModel.redirectHandler(value);
            }
            
        };
    }
    
    // -- IMPL

    /**
     * Call this method to initiate the download or redirect.
     */
    public void initiate(
            final @NonNull AjaxRequestTarget target) {
        
        val url = ActionResultResponseHandlingStrategy.expanded(getCallbackUrl().toString());
        val js = javascriptFor(url);

        // the timeout is needed to let Wicket release the channel
        target.appendJavaScript(String.format("setTimeout(%s, 100);", js));
    }

    @Override
    public void onRequest() {
        IRequestHandler handler = getRequestHandler();
        if(handler != null) {
            getComponent().getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
        }
    }
    
    public abstract boolean needsDeferring();
    protected abstract IRequestHandler getRequestHandler();
    protected abstract String javascriptFor(String url);
    
    // -- RESULT MEMOIZATION
    
    private transient ManagedObject resultAdapter = null;
    
    protected Object getResultValue() {
        if(resultAdapter==null) {
            resultAdapter = actionModel.execute();
        }
        return ManagedObjects.UnwrapUtil.single(resultAdapter);
    }
    
    protected void freeResultValue() {
        resultAdapter = null;
    }
    
    // -- HELPER
    
    private static String javascriptFor_newWindow(AjaxDeferredBehaviour deferredBehaviour, String url) {
        val parsedUrl = Url.parse(url);
        val requestCycle = deferredBehaviour.getComponent().getRequestCycle();
        val urlRenderer = requestCycle.getUrlRenderer();
        val fullUrl = urlRenderer.renderFullUrl(parsedUrl);
        return "function(){Wicket.Event.publish(Isis.Topic.OPEN_IN_NEW_TAB, '" + fullUrl + "');}";
    }
    
    private static String javascriptFor_sameWindow(AjaxDeferredBehaviour deferredBehaviour, String url) {
        return "\"window.location.href='" + url + "'\"";
    }

    
}
