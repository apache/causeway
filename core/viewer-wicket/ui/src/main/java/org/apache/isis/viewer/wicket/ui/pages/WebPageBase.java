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
package org.apache.isis.viewer.wicket.ui.pages;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.config.beans.WebAppConfigBean;
import org.apache.isis.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.common.CommonContextUtils;
import org.apache.isis.webapp.context.IsisWebAppCommonContext;

/**
 * Provides all the system dependencies for sub-classes.
 * @since 2.0
 */
public abstract class WebPageBase extends WebPage implements IsisWebAppCommonContext.Delegating {

    private static final long serialVersionUID = 1L;
    
    private transient WebAppConfigBean webAppConfigBean;
    private transient PageClassRegistry pageClassRegistry;
    private transient IsisWebAppCommonContext commonContext;
    private transient IsisSessionFactory isisSessionFactory;
    
    protected WebPageBase(PageParameters parameters) {
        super(parameters);
    }
    
    protected WebPageBase(final IModel<?> model) {
        super(model);
    }
    
    @Override
    public IsisWebAppCommonContext getCommonContext() {
        commonContext = CommonContextUtils.computeIfAbsent(commonContext);
        
        this.webAppConfigBean = commonContext.lookupServiceElseFail(WebAppConfigBean.class);
        this.pageClassRegistry = commonContext.lookupServiceElseFail(PageClassRegistry.class);
        
        return commonContext;
    }
    
    public WebAppConfigBean getWebAppConfigBean() {
        if(webAppConfigBean==null) {
            webAppConfigBean = getCommonContext().lookupServiceElseFail(WebAppConfigBean.class);
        }
        return webAppConfigBean;
    }
    
    public PageClassRegistry getPageClassRegistry() {
        if(pageClassRegistry==null) {
            pageClassRegistry = getCommonContext().lookupServiceElseFail(PageClassRegistry.class);
        }
        return pageClassRegistry;
    }
    
    public IsisSessionFactory getIsisSessionFactory() {
        if(isisSessionFactory==null) {
            isisSessionFactory = getCommonContext().lookupServiceElseFail(IsisSessionFactory.class);
        }
        return isisSessionFactory;
    }
    
    
}
