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


package org.apache.isis.viewer.scimpi.dispatcher.view.display;

import java.util.Iterator;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSession;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;


public abstract class AbstractTableView extends AbstractElementProcessor {

    @Override
    public void process(Request request) {
        RequestContext context = request.getContext();

        ObjectAdapter collection;
        String parentObjectId = null;
        boolean isFieldEditable = false;
        String field = request.getOptionalProperty(FIELD);
        ObjectSpecification elementSpec;
        if (field != null) {
            String objectId = request.getOptionalProperty(OBJECT);
            ObjectAdapter object = context.getMappedObjectOrResult(objectId);
            if (object == null) {
                throw new ScimpiException("No object for result or " + objectId);
            }
            ObjectAssociation objectField = object.getSpecification().getAssociation(field);
            if (!objectField.isOneToManyAssociation()) {
                throw new ScimpiException("Field " + objectField.getId() + " is not a collection");
            }
            isFieldEditable = objectField.isUsable(IsisContext.getAuthenticationSession(), object).isAllowed();
            getPersistenceSession().resolveField(object, objectField);
            collection = objectField.get(object);
            TypeOfFacet facet = objectField.getFacet(TypeOfFacet.class);
            elementSpec = facet.valueSpec();
            parentObjectId = objectId == null ? context.mapObject(object, Scope.REQUEST) : objectId;
        } else {
            String id = request.getOptionalProperty(COLLECTION);
            collection = context.getMappedObjectOrResult(id);
            elementSpec = collection.getElementSpecification();
        }

        String summary = request.getOptionalProperty("summary");
        String rowClassesList = request.getOptionalProperty(ROW_CLASSES, ODD_ROW_CLASS + "|" + EVEN_ROW_CLASS);
        String[] rowClasses = null;
        if (rowClassesList.length() > 0) {
            rowClasses = rowClassesList.split("[,|/]");
        }

        List<ObjectAssociation> allFields = elementSpec.getAssociations(
                ObjectAssociationFilters.STATICALLY_VISIBLE_ASSOCIATIONS);
        TableContentWriter rowBuilder = createRowBuilder(request, context, isFieldEditable ? parentObjectId : null, allFields, collection);
        write(request, collection, summary, rowBuilder, rowClasses);

    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected abstract TableContentWriter createRowBuilder(
            final Request request,
            RequestContext context,
            final String parent,
            final List<ObjectAssociation> allFields, ObjectAdapter collection);

    public static void write(Request request, ObjectAdapter collection, String summary, TableContentWriter rowBuilder, String[] rowClasses) {
        RequestContext context = request.getContext();

        request.appendHtml("<table summary=\"" + summary + "\">");
        rowBuilder.writeHeaders(request);
        rowBuilder.writeFooters(request);

        request.appendHtml("<tbody>");
        CollectionFacet facet = collection.getSpecification().getFacet(CollectionFacet.class);
        Iterator<ObjectAdapter> iterator = facet.iterator(collection);
        int row = 1;
        while (iterator.hasNext()) {
            ObjectAdapter element = iterator.next();

            context.addVariable("row", "" + (row), Scope.REQUEST);
            String cls = "";
            if (rowClasses != null) {
                cls = " class=\"" + rowClasses[row % rowClasses.length] + "\"";
            }
            request.appendHtml("<tr" + cls + ">");
            rowBuilder.writeElement(request, context, element);
            request.appendHtml("</tr>");
            row++;
        }
        request.appendHtml("</tbody>");
        request.appendHtml("</table>");
    }

    @Override
    public String getName() {
        return "table";
    }

}

