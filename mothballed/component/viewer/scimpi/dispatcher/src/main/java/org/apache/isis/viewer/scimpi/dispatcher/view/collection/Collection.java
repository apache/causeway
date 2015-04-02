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

package org.apache.isis.viewer.scimpi.dispatcher.view.collection;

import java.util.Iterator;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ResolveFieldUtil;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request.RepeatMarker;

public class Collection extends AbstractElementProcessor {

    @Override
    public void process(final Request request) {
        final RequestContext context = request.getContext();

        ObjectAdapter collection;

        final String field = request.getOptionalProperty(FIELD);
        if (field != null) {
            final String id = request.getOptionalProperty(OBJECT);
            final ObjectAdapter object = context.getMappedObjectOrResult(id);
            final ObjectAssociation objectField = object.getSpecification().getAssociation(field);
            if (!objectField.isOneToManyAssociation()) {
                throw new ScimpiException("Field " + objectField.getId() + " is not a collection");
            }
            ResolveFieldUtil.resolveField(object, objectField);
            collection = objectField.get(object);
        } else {
            final String id = request.getOptionalProperty(COLLECTION);
            collection = context.getMappedObjectOrResult(id);
        }

        final RepeatMarker marker = request.createMarker();

        final String variable = request.getOptionalProperty(ELEMENT_NAME);
        final String scopeName = request.getOptionalProperty(SCOPE);
        final Scope scope = RequestContext.scope(scopeName, Scope.REQUEST);
        final String rowClassesList = request.getOptionalProperty(ROW_CLASSES, ODD_ROW_CLASS + "|" + EVEN_ROW_CLASS);
        String[] rowClasses = new String[0];
        if (rowClassesList != null) {
            rowClasses = rowClassesList.split("[,|/]");
        }

        final CollectionFacet facet = collection.getSpecification().getFacet(CollectionFacet.class);
        if (facet.size(collection) == 0) {
            request.skipUntilClose();
        } else {
            final Iterator<ObjectAdapter> iterator = facet.iterator(collection);
            int row = 0;
            while (iterator.hasNext()) {
                final ObjectAdapter element = iterator.next();
                context.addVariable("row", "" + (row + 1), Scope.REQUEST);
                if (rowClassesList != null) {
                    context.addVariable("row-class", rowClasses[row % rowClasses.length], Scope.REQUEST);
                }
                context.addVariable(variable, context.mapObject(element, scope), scope);
                marker.repeat();
                request.processUtilCloseTag();
                row++;
            }
        }
    }

    @Override
    public String getName() {
        return COLLECTION;
    }

}
