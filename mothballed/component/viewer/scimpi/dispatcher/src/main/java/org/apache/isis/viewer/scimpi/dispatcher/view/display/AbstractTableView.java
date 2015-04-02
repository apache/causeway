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
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ResolveFieldUtil;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public abstract class AbstractTableView extends AbstractElementProcessor {

    // REVIEW: should provide this rendering context, rather than hardcoding.
    // the net effect currently is that class members annotated with
    // @Hidden(where=Where.ALL_TABLES) or @Disabled(where=Where.ALL_TABLES) will indeed
    // be hidden from all tables but will be visible/enabled (perhaps incorrectly) 
    // if annotated with Where.PARENTED_TABLE or Where.STANDALONE_TABLE
    private final Where where = Where.ALL_TABLES;

    @Override
    public void process(final Request request) {
        final RequestContext context = request.getContext();

        ObjectAdapter collection;
        String parentObjectId = null;
        boolean isFieldEditable = false;
        final String field = request.getOptionalProperty(FIELD);
        final String tableClass = request.getOptionalProperty(CLASS);
        ObjectSpecification elementSpec;
        String tableId;
        if (field != null) {
            final String objectId = request.getOptionalProperty(OBJECT);
            final ObjectAdapter object = context.getMappedObjectOrResult(objectId);
            if (object == null) {
                throw new ScimpiException("No object for result or " + objectId);
            }
            final ObjectAssociation objectField = object.getSpecification().getAssociation(field);
            if (!objectField.isOneToManyAssociation()) {
                throw new ScimpiException("Field " + objectField.getId() + " is not a collection");
            }
            isFieldEditable = objectField.isUsable(IsisContext.getAuthenticationSession(), object, where).isAllowed();
            ResolveFieldUtil.resolveField(object, objectField);
            collection = objectField.get(object);
            final TypeOfFacet facet = objectField.getFacet(TypeOfFacet.class);
            elementSpec = facet.valueSpec();
            parentObjectId = objectId == null ? context.mapObject(object, Scope.REQUEST) : objectId;
            tableId = request.getOptionalProperty(ID, field);
        } else {
            final String id = request.getOptionalProperty(COLLECTION);
            collection = context.getMappedObjectOrResult(id);
            elementSpec = collection.getElementSpecification();
            tableId = request.getOptionalProperty(ID, collection.getElementSpecification().getShortIdentifier());
        }

        final String summary = request.getOptionalProperty("summary");
        final String rowClassesList = request.getOptionalProperty(ROW_CLASSES, ODD_ROW_CLASS + "|" + EVEN_ROW_CLASS);
        String[] rowClasses = null;
        if (rowClassesList.length() > 0) {
            rowClasses = rowClassesList.split("[,|/]");
        }

        final List<ObjectAssociation> allFields = elementSpec.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.VISIBLE_AT_LEAST_SOMETIMES);
        final TableContentWriter rowBuilder = createRowBuilder(request, context, isFieldEditable ? parentObjectId : null, allFields, collection);
        write(request, collection, summary, rowBuilder, tableId, tableClass, rowClasses);

    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected abstract TableContentWriter createRowBuilder(final Request request, RequestContext context, final String parent, final List<ObjectAssociation> allFields, ObjectAdapter collection);

    public static void write(
            final Request request,
            final ObjectAdapter collection,
            final String summary,
            final TableContentWriter rowBuilder,
            final String tableId,
            final String tableClass,
            final String[] rowClasses) {
        final RequestContext context = request.getContext();

        final String summarySegment = summary == null ? "" : (" summary=\"" + summary + "\"");
        final String idSegment = tableId == null ? "" : (" id=\"" + tableId + "\""); 
        final String classSegment = tableClass == null ? "" : (" class=\"" + tableClass + "\"");
        request.appendHtml("<table" + idSegment + classSegment + summarySegment + ">");
        rowBuilder.writeCaption(request);
        rowBuilder.writeHeaders(request);
        rowBuilder.writeFooters(request);

        request.appendHtml("<tbody>");
        final CollectionFacet facet = collection.getSpecification().getFacet(CollectionFacet.class);
        final Iterator<ObjectAdapter> iterator = facet.iterator(collection);
        int row = 1;
        while (iterator.hasNext()) {
            final ObjectAdapter element = iterator.next();

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
        
        rowBuilder.tidyUp();
    }

    @Override
    public String getName() {
        return "table";
    }

}
