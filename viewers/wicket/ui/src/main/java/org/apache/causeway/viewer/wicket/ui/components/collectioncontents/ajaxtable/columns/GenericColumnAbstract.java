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
package org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.tabular.DataRow;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataRowWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.app.registry.HasComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

/**
 * Represents a {@link AbstractColumn} within a
 * {@link AjaxFallbackDefaultDataTable}.
 * <p>
 * Part of the implementation of
 * {@link org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanel}.
 */
public abstract class GenericColumnAbstract
extends AbstractColumn<DataRow, String>
implements GenericColumn, HasMetaModelContext {
    private static final long serialVersionUID = 1L;

    private transient ComponentFactoryRegistry componentRegistry;
    private final Class<?> elementClass;
    private transient ObjectSpecification elementType;

    protected GenericColumnAbstract(
            final ObjectSpecification elementType,
            final String columnName) {
        this(elementType, Model.of(columnName), null);
    }

    protected GenericColumnAbstract(
            final ObjectSpecification elementType,
            final IModel<String> columnNameModel,
            final String sortColumn) {
        super(columnNameModel, sortColumn);
        this.elementType = elementType;
        this.elementClass = elementType.getCorrespondingClass();
    }

    @Override
    public final void populateItem(
            final Item<ICellPopulator<DataRow>> cellItem,
            final String componentId,
            final IModel<DataRow> rowModel) {
        cellItem.add(createCellComponent(componentId, (DataRowWkt)rowModel));
        if(this instanceof ActionColumn) {
            Wkt.cssAppend(cellItem, "action-column");
        }
        if(this instanceof TitleColumn) {
            Wkt.cssAppend(cellItem, "title-column");
            if(((TitleColumn)this).isTitleSuppressed()) {
                // governed via CSS: render icon slightly larger, when title-suppressed e.g. 1.25em vs 0.9em (default)
                Wkt.cssAppend(cellItem, "title-suppressed");
            }
        } else if(this instanceof ToggleboxColumn) {
            Wkt.cssAppend(cellItem, "togglebox-column");
            final MarkupContainer row = cellItem.getParent().getParent();
            row.setOutputMarkupId(true);
        }
    }

    protected abstract Component createCellComponent(final String componentId, final DataRowWkt dataRowWkt);

    @Override
    public final ObjectSpecification elementType() {
        synchronized(this) {
            if(elementType==null) {
                this.elementType = MetaModelContext.instanceElseFail().specForTypeElseFail(elementClass);
            }
        }
        return elementType;
    }

    protected ComponentFactory findComponentFactory(final UiComponentType uiComponentType, final IModel<?> model) {
        return getComponentRegistry().findComponentFactory(uiComponentType, model);
    }

    protected ComponentFactoryRegistry getComponentRegistry() {
        if(componentRegistry==null) {
            var componentFactoryRegistryAccessor = (HasComponentFactoryRegistry) Application.get();
            componentRegistry = componentFactoryRegistryAccessor.getComponentFactoryRegistry();
        }
        return componentRegistry;
    }

    protected String translate(final String raw) {
        return getMetaModelContext().getTranslationService().translate(TranslationContext.empty(), raw);
    }

}
