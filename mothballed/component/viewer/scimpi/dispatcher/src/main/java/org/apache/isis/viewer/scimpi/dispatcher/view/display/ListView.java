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

    @Override
    public String checkFieldType(final ObjectAssociation objectField) {
        return objectField.isOneToManyAssociation() ? null : "is not a collection";
    }

    @Override
    public void process(final Request request, final ObjectAdapter object) {
        final String linkRowView = request.getOptionalProperty(LINK_VIEW);
        final String linkObjectName = request.getOptionalProperty(ELEMENT_NAME, RequestContext.RESULT);
        final String linkObjectScope = request.getOptionalProperty(SCOPE, Scope.INTERACTION.toString());
        LinkedObject linkedRow = null;
        if (linkRowView != null) {
            linkedRow = new LinkedObject(linkObjectName, linkObjectScope, request.getContext().fullUriPath(linkRowView));
        }
        final String bulletType = request.getOptionalProperty("type");
        write(request, object, linkedRow, bulletType);
    }

    public static void write(final Request request, final ObjectAdapter collection, final LinkedObject linkRow, final String bulletType) {

        if (bulletType == null) {
            request.appendHtml("<ol>");
        } else {
            request.appendHtml("<ul type=\"" + bulletType + "\">");
        }

        final CollectionFacet facet = collection.getSpecification().getFacet(CollectionFacet.class);
        final Iterator<ObjectAdapter> iterator = facet.iterator(collection);
        while (iterator.hasNext()) {
            final ObjectAdapter element = iterator.next();

            request.appendHtml("<li>");
            if (linkRow != null) {
                final Scope scope = linkRow == null ? Scope.INTERACTION : RequestContext.scope(linkRow.getScope());
                RequestContext context = request.getContext();
                final String rowId = context.mapObject(element, scope);
                request.appendHtml("<a class=\"item-select\" href=\"" + linkRow.getForwardView() + "?" + linkRow.getVariable()
                        + "=" + rowId + context.encodedInteractionParameters() + "\">");
            }
            request.appendAsHtmlEncoded(element.titleString());
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

    @Override
    public String getName() {
        return "list";
    }

}
