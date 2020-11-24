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

package org.apache.isis.core.metamodel.facets.object.objectvalidprops.impl;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.objectvalidprops.ObjectValidPropertiesFacetAbstract;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public class ObjectValidPropertiesFacetImpl extends ObjectValidPropertiesFacetAbstract {

    // REVIEW: should provide this rendering context, rather than hardcoding.
    // the net effect currently is that class members annotated with
    // @Hidden(where=Where.ANYWHERE) or @Disabled(where=Where.ANYWHERE) will indeed
    // be hidden/disabled, but will be visible/enabled (perhaps incorrectly)
    // for any other value for Where
    private final Where where = Where.ANYWHERE;

    public ObjectValidPropertiesFacetImpl(final FacetHolder holder) {
        super(holder);
    }

    @Override
    public String invalidReason(
            final ObjectValidityContext context) {
        final StringBuilder buf = new StringBuilder();
        final ManagedObject adapter = context.getTarget();

        adapter.getSpecification().streamAssociations(MixedIn.EXCLUDED)
        .filter(ObjectAssociation.Predicates.PROPERTIES)
        .filter(property->property.isVisible(adapter, context.getInitiatedBy(), where).isVetoed()) // ignore hidden properties
        .filter(property->property.isUsable(adapter, context.getInitiatedBy(), where).isVetoed())  // ignore disabled properties
        .forEach(property->{
            final OneToOneAssociation otoa = (OneToOneAssociation) property;
            final ManagedObject value = otoa.get(adapter, context.getInitiatedBy());
            if (otoa.isAssociationValid(adapter, value, context.getInitiatedBy()).isVetoed()) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(property.getName());
            }    
        });
        if (buf.length() > 0) {
            return "Invalid properties: " + buf.toString();
        }
        return null;
    }

}
