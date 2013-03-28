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

package org.apache.isis.viewer.html.action.view;

import java.util.List;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.viewer.html.component.Block;
import org.apache.isis.viewer.html.component.Component;
import org.apache.isis.viewer.html.component.ComponentFactory;
import org.apache.isis.viewer.html.component.ViewPane;
import org.apache.isis.viewer.html.context.Context;
import org.apache.isis.viewer.html.request.Request;

public class ObjectView extends ObjectViewAbstract {

    // REVIEW: confirm this rendering context
    private final Where where = Where.OBJECT_FORMS;

    @Override
    protected boolean addObjectToHistory() {
        return true;
    }

    @Override
    protected void doExecute(final Context context, final ViewPane content, final ObjectAdapter adapter, final String field) {
        final String id = context.mapObject(adapter);
        final ObjectSpecification specification = adapter.getSpecification();

        final AuthenticationSession authenticationSession = getAuthenticationSession();

        createObjectView(context, adapter, content, id);

        // // TODO: this test should be done by the ImmutableFacetFactory
        // installing an immutableFacet on every
        // // member
        // final boolean immutable =
        // ImmutableFacetUtils.isAlwaysImmutable(specification)
        // || (adapter.isPersistent() &&
        // ImmutableFacetUtils.isImmutableOncePersisted(specification));

        boolean atLeastOneFieldVisibleAndEditable = false;
        final List<ObjectAssociation> flds = specification.getAssociations();
        for (int i = 0; i < flds.size(); i++) {
            if (flds.get(i).isVisible(authenticationSession, adapter, where).isAllowed() && flds.get(i).isUsable(authenticationSession, adapter, where).isAllowed()) {
                atLeastOneFieldVisibleAndEditable = true;
                break;
            }
        }
        if (/* !immutable && */atLeastOneFieldVisibleAndEditable) {
            content.add(context.getComponentFactory().createEditOption(id));
        }

        context.setObjectCrumb(adapter);
    }

    private void createObjectView(final Context context, final ObjectAdapter object, final ViewPane pane, final String id) {

        final ObjectSpecification specification = object.getSpecification();
        final List<ObjectAssociation> visibleFields = specification.getAssociations(ObjectAssociationFilters.dynamicallyVisible(getAuthenticationSession(), object, where));
        for (int i = 0; i < visibleFields.size(); i++) {
            final ObjectAssociation field = visibleFields.get(i);

            final ComponentFactory factory = context.getComponentFactory();
            final Block fieldBlock = factory.createBlock("field", field.getDescription());
            fieldBlock.add(factory.createInlineBlock("label", field.getName(), null));
            fieldBlock.add(factory.createInlineBlock("separator", ":  ", null));

            getPersistenceSession().resolveField(object, field);

            // ordering is important here;
            // look at parseable fields before objects
            final ObjectAdapter associatedObject = field.get(object);
            Component component = null;
            if (field.getSpecification().isParseable()) {
                component = factory.createParseableField(field, associatedObject, false);
            } else if (field.isOneToOneAssociation()) {
                if (associatedObject == null) {
                    component = factory.createInlineBlock("value", "", null);
                    fieldBlock.add(component);
                } else {
                    // previously there was a called to resolveImmediately here
                    // on the
                    // associated object, but it has been removed (presumably we
                    // don't
                    // want to force eager loading).
                    final String elementId = context.mapObject(associatedObject);
                    component = factory.createObjectIcon(field, associatedObject, elementId, "value");
                }
            } else if (field.isOneToManyAssociation()) {
                component = factory.createCollectionIcon(field, associatedObject, id);
            } else {
                throw new UnknownTypeException(field);
            }
            fieldBlock.add(component);

            pane.add(fieldBlock);
        }
    }

    @Override
    public String name() {
        return Request.OBJECT_COMMAND;
    }

}
