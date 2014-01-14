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

import java.net.URL;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;

/**
 * @see https://cwiki.apache.org/confluence/display/WICKET/AJAX+update+and+file+download+in+one+blow
 * 
 * @author Sven Meier
 * @author Ernesto Reinaldo Barreiro (reiern70@gmail.com)
 * @author Jordi Deu-Pons (jordi@jordeu.net)
 */
public abstract class AjaxRedirect extends AjaxDeferredBehaviour {

    private static final long serialVersionUID = 1L;

    public AjaxRedirect() {
        super();
    }

    public AjaxRedirect(boolean addAntiCache) {
        super(addAntiCache);
    }

    @Override
    protected IRequestHandler getRequestHandler() {
        final java.net.URL url = getRedirectUrl();
        IRequestHandler handler = new RedirectRequestHandler(url.toString());
        return handler;
    }

    /**
     * Hook method providing the actual URL.
     */
    protected abstract URL getRedirectUrl();
}