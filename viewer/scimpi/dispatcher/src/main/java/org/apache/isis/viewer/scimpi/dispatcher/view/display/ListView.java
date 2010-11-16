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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractObjectProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.view.field.LinkedObject;


public class ListView extends AbstractObjectProcessor {

    public String checkFieldType(ObjectAssociation objectField) {
        return objectField.isOneToManyAssociation() ? null : "is not a collection";
    }

    public void process(Request request, ObjectAdapter object) {
        String linkRowView = request.getOptionalProperty(LINK);
        String linkObjectName = request.getOptionalProperty(ELEMENT_NAME, RequestContext.RESULT);
        String linkObjectScope = request.getOptionalProperty(SCOPE, Scope.INTERACTION.toString());
        LinkedObject linkedRow = null;
        if (linkRowView != null) {
            linkedRow = new LinkedObject(linkObjectName, linkObjectScope, request.getContext().fullUriPath(linkRowView));
        }
        String bulletType = request.getOptionalProperty("type");
        write(request, object, linkedRow, bulletType);
    }

    public static void write(
            Request request,
            ObjectAdapter collection,
            LinkedObject linkRow,
            String bulletType) {

        if (bulletType == null) {
            request.appendHtml("<ol>");
        } else {
            request.appendHtml("<ul type=\"" + bulletType + "\">");
        }

        CollectionFacet facet = (CollectionFacet) collection.getSpecification().getFacet(CollectionFacet.class);
        Iterator<ObjectAdapter> iterator = facet.iterator(collection);
        while (iterator.hasNext()) {
            ObjectAdapter element = (ObjectAdapter) iterator.next();

            request.appendHtml("<li>");
            if (linkRow != null) {
                Scope scope = linkRow == null ? Scope.INTERACTION : RequestContext.scope(linkRow.getScope());
                String rowId = request.getContext().mapObject(element, scope);
                request.appendHtml("<a class=\"item-select\" href=\"" + linkRow.getForwardView() + "?" + linkRow.getVariable() + "=" + rowId
                        + "\">");
            }
            request.appendHtml(element.titleString());
            if (linkRow != null) {
                request.appendHtml("</a>");
            }

            request.appendHtml("</li>\n");
        }
        if (bulletType == null) {
            request.appendHtml("</ol>");
        } else {
            request.appendHtml("</ul>");
        }

    }

    public String getName() {
        return "list";
    }

}

