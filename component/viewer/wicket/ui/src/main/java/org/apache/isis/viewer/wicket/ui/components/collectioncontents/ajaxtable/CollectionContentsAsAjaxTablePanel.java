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

import java.util.Collections;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.common.SelectionHandler;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ColumnAbstract;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterPropertyColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterTitleColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuBuilder;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} that represents a {@link EntityCollectionModel
 * collection of entity}s rendered using {@link AjaxFallbackDefaultDataTable}.
 */
public class CollectionContentsAsAjaxTablePanel extends PanelAbstract<EntityCollectionModel> {

    private static final Predicate<ObjectAction> BULK = Filters.asPredicate(ObjectAction.Filters.bulk());

    private static final long serialVersionUID = 1L;

    private static final String ID_TABLE = "table";
    private static final String ID_ENTITY_ACTIONS = "entityActions";

    private DataTable<ObjectAdapter,String> dataTable;

    public CollectionContentsAsAjaxTablePanel(final String id, final EntityCollectionModel model) {
        super(id, model);

        buildGui();
    }

    private void buildGui() {
        final EntityCollectionModel model = getModel();

        final List<IColumn<ObjectAdapter,String>> columns = Lists.newArrayList();

        List<ObjectAction> bulkActions = determineBulkActions();

        addToggleboxColumnIfRequired(columns, bulkActions);
        addTitleColumn(columns, model.getParentObjectAdapterMemento());
        addPropertyColumnsIfRequired(columns);

        final SortableDataProvider<ObjectAdapter,String> dataProvider = new CollectionContentsSortableDataProvider(model);
        dataTable = new MyAjaxFallbackDefaultDataTable<ObjectAdapter,String>(ID_TABLE, columns, dataProvider, model.getPageSize());

        buildEntityActionsGui(bulkActions);

        addOrReplace(dataTable);
    }
    
    private void addToggleboxColumnIfRequired(final List<IColumn<ObjectAdapter,String>> columns, List<ObjectAction> bulkActions) {
        final EntityCollectionModel model = getModel();
        if(bulkActions.isEmpty() || model.isParented()) {
            return;
        }
        
        columns.add(new ObjectAdapterToggleboxColumn(new SelectionHandler() {
            
            private static final long serialVersionUID = 1L;

            @Override
            public void onSelected(final Component context, final ObjectAdapter selectedAdapter) {
                model.toggleSelectionOn(selectedAdapter);
            }
        }));
    }

    private void buildEntityActionsGui(List<ObjectAction> bulkActions) {
        final EntityCollectionModel model = getModel();
        
        if(bulkActions.isEmpty() || model.isParented()) {
            permanentlyHide(ID_ENTITY_ACTIONS);
            return;
        }
        
        if(!bulkActions.isEmpty()) {
            final CssMenuLinkFactory linkFactory = new BulkActionsLinkFactory(model, dataTable);

            final CssMenuBuilder cssMenuBuilder = new CssMenuBuilder(null, getServiceAdapters(), bulkActions, linkFactory);
            // TODO: i18n
            final CssMenuPanel cssMenuPanel = cssMenuBuilder.buildPanel(ID_ENTITY_ACTIONS, "Actions");

            this.addOrReplace(cssMenuPanel);
        } else {
            permanentlyHide(ID_ENTITY_ACTIONS);
        }
    }

    private List<ObjectAction> determineBulkActions() {
        final EntityCollectionModel model = getModel();
        
        if(model.isParented()) {
            return Collections.emptyList();
        }
        
        final ObjectSpecification typeSpec = model.getTypeOfSpecification();
        
        List<ObjectAction> objectActions = typeSpec.getObjectActions(ActionType.USER, Contributed.INCLUDED, Filters.<ObjectAction>any());
        
        if ( isExploring() || isPrototyping()) {
            List<ObjectAction> explorationActions = typeSpec.getObjectActions(ActionType.EXPLORATION, Contributed.INCLUDED, Filters.<ObjectAction>any());
            List<ObjectAction> prototypeActions = typeSpec.getObjectActions(ActionType.PROTOTYPE, Contributed.INCLUDED, Filters.<ObjectAction>any());
            objectActions.addAll(explorationActions);
            objectActions.addAll(prototypeActions);
        }
        if (isDebugMode()) {
            List<ObjectAction> debugActions = typeSpec.getObjectActions(ActionType.DEBUG, Contributed.INCLUDED, Filters.<ObjectAction>any());
            objectActions.addAll(debugActions);
        }

        List<ObjectAction> flattenedActions = objectActions;
        
        return Lists.newArrayList(Iterables.filter(flattenedActions, BULK));
    }

    
    public boolean isExploring() {
        return IsisContext.getDeploymentType().isExploring();
    }
    public boolean isPrototyping() {
        return IsisContext.getDeploymentType().isPrototyping();
    }

    /**
     * Protected so can be overridden in testing if required.
     */
    protected boolean isDebugMode() {
        // TODO: need to figure out how to switch into debug mode;
        // probably call a Debug toggle page, and stuff into
        // Session.getMetaData()
        return true;
    }


    private static void addTitleColumn(final List<IColumn<ObjectAdapter,String>> columns, ObjectAdapterMemento parentAdapterMementoIfAny) {
        columns.add(new ObjectAdapterTitleColumn(parentAdapterMementoIfAny));
    }

    private void addPropertyColumnsIfRequired(final List<IColumn<ObjectAdapter,String>> columns) {
        final ObjectSpecification typeOfSpec = getModel().getTypeOfSpecification();

        final ObjectSpecification parentSpecIfAny = 
                getModel().isParented() 
                    ? getModel().getParentObjectAdapterMemento().getObjectAdapter(ConcurrencyChecking.NO_CHECK).getSpecification() 
                    : null;
        
        @SuppressWarnings("unchecked")
        final Filter<ObjectAssociation> filter = Filters.and(
                ObjectAssociation.Filters.PROPERTIES, 
                ObjectAssociation.Filters.staticallyVisible(getModel().isParented()? Where.PARENTED_TABLES: Where.STANDALONE_TABLES),
                associationDoesNotReferenceParent(parentSpecIfAny));
        final List<? extends ObjectAssociation> propertyList = typeOfSpec.getAssociations(Contributed.INCLUDED, filter);
        for (final ObjectAssociation property : propertyList) {
            final ColumnAbstract<ObjectAdapter> nopc = createObjectAdapterPropertyColumn(property);
            columns.add(nopc);
        }
    }

    static Filter<ObjectAssociation> associationDoesNotReferenceParent(final ObjectSpecification parentSpec) {
        if(parentSpec == null) {
            return Filters.any();
        }
        return new Filter<ObjectAssociation>() {
            @Override
            public boolean accept(ObjectAssociation association) {
                final HiddenFacet facet = association.getFacet(HiddenFacet.class);
                if(facet == null) {
                    return true;
                }
                if (facet.where() != Where.REFERENCES_PARENT) {
                    return true;
                }
                final ObjectSpecification assocSpec = association.getSpecification();
                final boolean associationSpecIsOfParentSpec = parentSpec.isOfType(assocSpec);
                final boolean isVisible = !associationSpecIsOfParentSpec;
                return isVisible;
            }
        };
    }

    private ObjectAdapterPropertyColumn createObjectAdapterPropertyColumn(final ObjectAssociation property) {
        return new ObjectAdapterPropertyColumn(Model.of(property.getName()), property.getId(), property.getId());
    }

    @Override
    protected void onModelChanged() {
        buildGui();
    }

}
