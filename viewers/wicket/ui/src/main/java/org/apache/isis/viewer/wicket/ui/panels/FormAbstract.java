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
package org.apache.isis.viewer.wicket.ui.panels;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;

public abstract class FormAbstract<T> extends Form<T>
implements ComponentFactoryRegistryAccessor, PageClassRegistryAccessor {

    private static final long serialVersionUID = 1L;

    private transient ComponentFactoryRegistry componentFactoryRegistry;
    private transient PageClassRegistry pageClassRegistry;
    private transient IsisAppCommonContext commonContext;

    protected FormAbstract(final String id) {
        super(id);
    }

    protected FormAbstract(final String id, final IModel<T> model) {
        super(id, model);
    }

    // -- DEPENDENCIES

    public final IsisAppCommonContext getCommonContext() {
        return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
    }

    @Override
    public final ComponentFactoryRegistry getComponentFactoryRegistry() {
        if(componentFactoryRegistry==null) {
            componentFactoryRegistry = ((ComponentFactoryRegistryAccessor) getApplication()).getComponentFactoryRegistry();
        }
        return componentFactoryRegistry;
    }

    @Override
    public final PageClassRegistry getPageClassRegistry() {
        if(pageClassRegistry==null) {
            pageClassRegistry = ((PageClassRegistryAccessor) getApplication()).getPageClassRegistry();
        }
        return pageClassRegistry;
    }

    protected final SpecificationLoader getSpecificationLoader() {
        return getCommonContext().getSpecificationLoader();
    }

    protected final ServiceRegistry getServiceRegistry() {
        return getCommonContext().getServiceRegistry();
    }

    protected final TranslationService getTranslationService() {
        return getCommonContext().getTranslationService();
    }

}
