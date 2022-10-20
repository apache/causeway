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
package org.apache.causeway.viewer.wicket.ui.panels;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.model.util.WktContext;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.app.registry.HasComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.pages.HasPageClassRegistry;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;

public abstract class FormAbstract<T> extends Form<T>
implements
    HasCommonContext,
    HasComponentFactoryRegistry,
    HasPageClassRegistry {

    private static final long serialVersionUID = 1L;

    protected FormAbstract(final String id) {
        super(id);
    }

    protected FormAbstract(final String id, final IModel<T> model) {
        super(id, model);
    }

    // -- DEPENDENCIES

    private transient MetaModelContext mmc;
    @Override
    public final MetaModelContext getMetaModelContext() {
        return mmc = WktContext.computeIfAbsent(mmc);
    }

    private transient ComponentFactoryRegistry componentFactoryRegistry;
    @Override
    public final ComponentFactoryRegistry getComponentFactoryRegistry() {
        if(componentFactoryRegistry==null) {
            componentFactoryRegistry = ((HasComponentFactoryRegistry) getApplication()).getComponentFactoryRegistry();
        }
        return componentFactoryRegistry;
    }

    private transient PageClassRegistry pageClassRegistry;
    @Override
    public final PageClassRegistry getPageClassRegistry() {
        if(pageClassRegistry==null) {
            pageClassRegistry = ((HasPageClassRegistry) getApplication()).getPageClassRegistry();
        }
        return pageClassRegistry;
    }

}
