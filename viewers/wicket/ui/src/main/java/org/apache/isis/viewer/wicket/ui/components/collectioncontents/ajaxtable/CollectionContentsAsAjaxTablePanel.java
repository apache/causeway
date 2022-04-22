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
package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.model.Model;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.wicket.model.hints.IsisActionCompletedEvent;
import org.apache.isis.viewer.wicket.model.hints.IsisEnvelopeEvent;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel.Variant;
import org.apache.isis.viewer.wicket.ui.components.collection.bulk.MultiselectToggleProvider;
import org.apache.isis.viewer.wicket.ui.components.collection.count.CollectionCountProvider;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.GenericColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.GenericPropertyColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.GenericTitleColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.GenericToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage.ViewmodelRefreshedEvent;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import lombok.val;

/**
 * {@link PanelAbstract Panel} that represents a {@link EntityCollectionModel
 * collection of entity}s rendered using {@link AjaxFallbackDefaultDataTable}.
 */
public class CollectionContentsAsAjaxTablePanel
extends PanelAbstract<DataTableModel, EntityCollectionModel>
implements CollectionCountProvider {

    private static final long serialVersionUID = 1L;
    private static final String ID_TABLE = "table";

    public CollectionContentsAsAjaxTablePanel(final String id, final EntityCollectionModel model) {
        super(id, model);
    }

    @Override
    public Integer getCount() {
        return getModel().getDataTableModel().getElementCount();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    @Override
    protected void onModelChanged() {
        //buildGui();
    }

    // -- HELPER

    private EntityCollectionModel entityCollectionModel() {
        return getModel();
    }

    private void buildGui() {

        final List<GenericColumn> columns = _Lists.newArrayList();

        // multi select check boxes
        final MultiselectToggleProvider multiselectToggleProvider = getMultiselectToggleProvider();

        GenericToggleboxColumn toggleboxColumn = null;
        if(multiselectToggleProvider != null) {

            toggleboxColumn = multiselectToggleProvider.getToggleboxColumn();
            if(toggleboxColumn != null) {
                columns.add(toggleboxColumn);
            }

        }

        val collectionModel = entityCollectionModel();
        addTitleColumn(
                columns,
                collectionModel.getVariant(),
                getWicketViewerSettings().getMaxTitleLengthInParentedTables(),
                getWicketViewerSettings().getMaxTitleLengthInStandaloneTables());

        addPropertyColumnsIfRequired(columns);

        val dataProvider = new CollectionContentsSortableDataProvider(collectionModel);
        val dataTable = new IsisAjaxDataTable(
                ID_TABLE, columns, dataProvider, collectionModel.getPageSize(), toggleboxColumn);
        addOrReplace(dataTable);

        setOutputMarkupId(true);
    }

    int primed = 0;

    /**
     * XXX[ISIS-3005] Any action dialog submission on the same page will
     * result in a new {@link DataTableModel}, where any previously rendered check-boxes
     * run out of sync with their DataRowToggle model.
     * Hence we intercept such events and reset check-boxes to un-checked.
     */
    @Override public void onEvent(final IEvent<?> event) {
        _Casts.castTo(IsisEnvelopeEvent.class, event.getPayload())
        .ifPresent(envelopeEvent->{
            if(envelopeEvent.getLetter() instanceof IsisActionCompletedEvent) {
                _Debug.log("DETACHING");
                val dataTable = (IsisAjaxDataTable)get(ID_TABLE);
                ((CollectionContentsSortableDataProvider)dataTable.getDataProvider()).forceDetach();
                primed = 2;
            }
            if(envelopeEvent.getLetter() instanceof ViewmodelRefreshedEvent) {
                _Debug.log("EVENT");
                if(primed==2) {
                    primed--;
                } else if(primed==1) {
                    _Debug.log("ATTACHING");
                    //buildGui();
                    envelopeEvent.getTarget().add(this);
                    primed = 0;
                }
            }

        });
        super.onEvent(event);
    }

    private MultiselectToggleProvider getMultiselectToggleProvider() {
        Component component = this;
        while(component != null) {
            if(component instanceof MultiselectToggleProvider) {
                return (MultiselectToggleProvider) component;
            }
            component = component.getParent();
        }
        return null;
    }


    private void addTitleColumn(
            final List<GenericColumn> columns,
            final Variant variant,
            final int maxTitleParented,
            final int maxTitleStandalone) {

        val contextBookmark = entityCollectionModel().getParentObject().getBookmark()
                .orElse(null);

        final int maxTitleLength = getModel().getVariant().isParented()? maxTitleParented: maxTitleStandalone;
        columns.add(new GenericTitleColumn(
                super.getCommonContext(), variant, contextBookmark, maxTitleLength));
    }

    private void addPropertyColumnsIfRequired(final List<GenericColumn> columns) {

        val collectionModel = getModel();
        val elementTypeSpec = collectionModel.getElementType();
        if(elementTypeSpec == null) {
            return;
        }

        final ManagedObject parentObject = collectionModel.getParentObject();
        val memberIdentifier = collectionModel.getIdentifier();

        // add all ordered columns to the table
        elementTypeSpec.streamPropertiesForColumnRendering(memberIdentifier, parentObject)
        .map(this::createObjectAdapterPropertyColumn)
        .forEach(columns::add);

    }

    private GenericPropertyColumn createObjectAdapterPropertyColumn(final OneToOneAssociation property) {

        val collectionModel = getModel();

        final boolean escaped = true;

        final String parentTypeName = property.getDeclaringType().getLogicalTypeName();

        val commonContext = super.getCommonContext();

        return new GenericPropertyColumn(
                commonContext,
                collectionModel.getVariant(),
                Model.of(property.getCanonicalFriendlyName()),
                property.getId(),
                property.getId(),
                escaped,
                parentTypeName,
                property.getCanonicalDescription());
    }

}
