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

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.memento.Memento;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionModel.SingleResultsMode;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.util.Mementos;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.pages.action.ActionPage;

/**
 * Convenience adapter for building bespoke panels for process objects.
 */
public abstract class ProcessObjectPanelAbstract extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;
    
    private final Where where;

    public ProcessObjectPanelAbstract(final String id, final EntityModel model, Where where) {
        super(id, model);
        this.where = where;
    }

    protected EntityModel getEntityModel() {
        return getModel();
    }

    protected void addProperties(final MarkupContainer mc, final String idProperties, final String idProperty) {
        final EntityModel entityModel = getModel();
        final ObjectAdapter adapter = entityModel.getObject();
        final ObjectSpecification noSpec = adapter.getSpecification();

        final List<OneToOneAssociation> properties = visibleProperties(adapter, noSpec);

        final RepeatingView rv = new RepeatingView(idProperties);
        mc.add(rv);
        final List<PropertyMemento> mementos = buildPropertyMementos(properties);
        for (final PropertyMemento pm : mementos) {
            final WebMarkupContainer container = new WebMarkupContainer(rv.newChildId());
            rv.add(container);

            final ScalarModel scalarModel = entityModel.getPropertyModel(pm);
            getComponentFactoryRegistry().addOrReplaceComponent(container, idProperty, ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);
        }
    }

    @SuppressWarnings("unchecked")
    private List<OneToOneAssociation> visibleProperties(final ObjectAdapter adapter, final ObjectSpecification noSpec) {
        @SuppressWarnings("rawtypes")
        final List list = noSpec.getAssociations(visiblePropertyFilter(adapter));
        return new ArrayList<OneToOneAssociation>(list);
    }

    private Filter<ObjectAssociation> visiblePropertyFilter(final ObjectAdapter adapter) {
        return Filters.and(ObjectAssociationFilters.PROPERTIES, ObjectAssociationFilters.dynamicallyVisible(getAuthenticationSession(), adapter, where));
    }

    private List<PropertyMemento> buildPropertyMementos(final List<OneToOneAssociation> properties) {
        final List<PropertyMemento> mementos = Lists.transform(properties, Mementos.fromProperty());
        // we copy into a new array list otherwise we get lazy evaluation =
        // reference to a non-serializable object
        return Lists.newArrayList(mementos);
    }

    /**
     * Validates the form properties and domain object (object-level
     * validation).
     */
    protected boolean isValid(final Form<?> form) {

        // check properties are all valid
        if (form.hasError()) {
            return false;
        }

        // check object is valid

        // to perform object-level validation, we must apply the changes first
        // Contrast this with ActionPanel (for validating action arguments)
        // where
        // we do the validation prior to the execution of the action.
        final ObjectAdapter object = getEntityModel().getObject();
        final Memento snapshotToRollbackToIfInvalid = new Memento(object);

        getEntityModel().apply();
        final String invalidReasonIfAny = getEntityModel().getReasonInvalidIfAny();
        if (invalidReasonIfAny != null) {
            form.error(invalidReasonIfAny);
            snapshotToRollbackToIfInvalid.recreateObject();
            return false;
        }

        // ok
        return true;
    }

    /**
     * Executes action; expected to take no arguments.
     */
    protected void executeNoArgAction(final String actionId) {
        final ObjectSpecification typeOfSpec = getModel().getTypeOfSpecification();
        final ObjectAction action = typeOfSpec.getObjectAction(ActionType.USER, actionId);
        final ObjectAdapterMemento adapterMemento = getModel().getObjectAdapterMemento();
        final ActionMemento actionMemento = new ActionMemento(action);
        final ActionModel.Mode actionMode = ActionModel.determineMode(action);

        final ActionModel actionModel = ActionModel.create(adapterMemento, actionMemento, actionMode, SingleResultsMode.INLINE);
        setResponsePage(new ActionPage(actionModel));
    }

}
