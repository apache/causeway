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
package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns;

import org.apache.wicket.Application;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanel;

import lombok.val;

/**
 * Represents a {@link AbstractColumn} within a
 * {@link AjaxFallbackDefaultDataTable}.
 *
 * <p>
 * Part of the implementation of {@link CollectionContentsAsAjaxTablePanel}.
 */
public abstract class ColumnAbstract<T>
extends AbstractColumn<T,String> {

    private static final long serialVersionUID = 1L;

    private transient IsisAppCommonContext commonContext;
    private transient ComponentFactoryRegistry componentRegistry;


    public ColumnAbstract(final IsisAppCommonContext commonContext, final String columnName) {
        this(commonContext, Model.of(columnName), null);
    }

    public ColumnAbstract(final IsisAppCommonContext commonContext, final IModel<String> columnNameModel, final String sortColumn) {
        super(columnNameModel, sortColumn);
        this.commonContext = commonContext;
    }

    public IsisAppCommonContext getCommonContext() {
        return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
    }

    protected ComponentFactory findComponentFactory(final ComponentType componentType, final IModel<?> model) {
        return getComponentRegistry().findComponentFactory(componentType, model);
    }

    protected ComponentFactoryRegistry getComponentRegistry() {
        if(componentRegistry==null) {
            val componentFactoryRegistryAccessor = (ComponentFactoryRegistryAccessor) Application.get();
            componentRegistry = componentFactoryRegistryAccessor.getComponentFactoryRegistry();
        }
        return componentRegistry;
    }

}
