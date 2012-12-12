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

package org.apache.isis.viewer.html.task;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.Persistor;
import org.apache.isis.viewer.html.component.Page;
import org.apache.isis.viewer.html.context.Context;

public class AddItemToCollectionTask extends Task {
    private final OneToManyAssociation field;

    public AddItemToCollectionTask(final Context context, final ObjectAdapter target, final OneToManyAssociation fld) {
        super(context, "Add to collection", "", target, 1);
        names[0] = fld.getName();
        descriptions[0] = fld.getDescription();
        fieldSpecifications[0] = fld.getSpecification();
        initialState[0] = null;
        optional[0] = true;
        // TODO add defaults and options
        this.field = fld;
    }

    @Override
    public void checkForValidity(final Context context) {
        final ObjectAdapter target = getTarget(context);
        final ObjectAdapter[] parameters = getEntries(context);

        final Consent valueValid = field.isValidToAdd(target, parameters[0]);
        errors[0] = valueValid.getReason();
    }

    @Override
    public void checkInstances(final Context context, final ObjectAdapter[] objects) {
        final ObjectAdapter collection = field.get(getTarget(context));
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
        final ObjectAdapter target = getTarget(context);
        for (int i = 0; i < objects.length; i++) {
            final Consent valueValid = field.isValidToAdd(target, objects[i]);
            if (valueValid.isVetoed()) {
                objects[i] = null;
            } else if (facet.contains(collection, objects[i])) {
                objects[i] = null;
            }
        }
    }

    @Override
    public ObjectAdapter completeTask(final Context context, final Page page) {
        final ObjectAdapter targetAdapter = getTarget(context);
        final ObjectAdapter[] parameterAdapters = getEntries(context);
        field.addElement(targetAdapter, parameterAdapters[0]);

        if (targetAdapter.isTransient()) {
            getPersistenceSession().makePersistent(targetAdapter);
        }
        return targetAdapter;
    }

    @Override
    public boolean isEditing() {
        return true;
    }

    // /////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////

    private static Persistor getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}
