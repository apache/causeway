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
package org.apache.causeway.core.metamodel.facets.object.objectvalidprops.impl;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.objectvalidprops.ObjectValidPropertiesFacetAbstract;
import org.apache.causeway.core.metamodel.interactions.ObjectValidityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;

public class ObjectValidPropertiesFacetImpl
extends ObjectValidPropertiesFacetAbstract {

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

        adapter.getSpecification().streamProperties(MixedIn.EXCLUDED)
        .filter(property->property.isVisible(adapter, context.getInitiatedBy(), where).isVetoed()) // ignore hidden properties
        .filter(property->property.isUsable(adapter, context.getInitiatedBy(), where).isVetoed())  // ignore disabled properties
        .forEach(property->{
            final ManagedObject value = property.get(adapter, context.getInitiatedBy());
            if (property.isAssociationValid(adapter, value, context.getInitiatedBy()).isVetoed()) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(property.getFriendlyName(context::getTarget));
            }
        });
        if (buf.length() > 0) {
            return "Invalid properties: " + buf.toString();
        }
        return null;
    }

}
