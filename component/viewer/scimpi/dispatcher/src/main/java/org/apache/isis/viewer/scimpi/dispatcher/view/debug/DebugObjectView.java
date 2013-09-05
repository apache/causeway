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

package org.apache.isis.viewer.scimpi.dispatcher.view.debug;

import java.util.Collection;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractObjectProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.view.display.FieldValue;
import org.apache.isis.viewer.scimpi.dispatcher.view.field.LinkedObject;


public class DebugObjectView extends AbstractObjectProcessor {

    @Override
    public void process(final Request request, final ObjectAdapter object) {
        final String classString = " class=\"" + request.getOptionalProperty(CLASS, "form debug") + "\"";
        final String objectLink = request.getOptionalProperty(OBJECT + "-" + LINK_VIEW, request.getViewPath());
        // final String collectionLink = request.getOptionalProperty(COLLECTION + "-" + LINK_VIEW, request.getViewPath());
        final String oddRowClass = request.getOptionalProperty(ODD_ROW_CLASS);
        final String evenRowClass = request.getOptionalProperty(EVEN_ROW_CLASS);
        final boolean showIcons = request.isRequested(SHOW_ICON, true);

        ObjectSpecification specification = object.getSpecification();

        request.appendHtml("<div" + classString + ">");
        request.appendHtml("<div class=\"title\">");
        request.appendAsHtmlEncoded(specification.getSingularName() + " - " + specification.getFullIdentifier());
        request.appendHtml("</div>");

        Version version = object.getVersion();
        request.appendHtml("<div class=\"version\">");
        request.appendAsHtmlEncoded("#" + version.sequence() + " - " + version.getUser() + " (" + version.getTime() + ")" );
        request.appendHtml("</div>");

        final List<ObjectAssociation> fields = specification.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.ALL);

        int row = 1;
        for (int i = 0; i < fields.size(); i++) {
            final ObjectAssociation field = fields.get(i);
            /*
             * if (field.isVisible(IsisContext.getAuthenticationSession(), object).isVetoed()) { continue; }
             */
            String cls;
            if (row++ % 2 == 1) {
                cls = " class=\"field " + (oddRowClass == null ? ODD_ROW_CLASS : oddRowClass) + "\"";
            } else {
                cls = " class=\"field " + (evenRowClass == null ? EVEN_ROW_CLASS : evenRowClass) + "\"";
            }
            request.appendHtml("<div " + cls + "><span class=\"label\">");
            request.appendAsHtmlEncoded(field.getName());
            request.appendHtml(":</span>");
            
            final boolean isNotParseable =
                !fields.get(i).getSpecification().containsFacet(ParseableFacet.class);
            LinkedObject linkedObject = null;
            if (isNotParseable) {
     //           linkedObject = new LinkedObject(field.isOneToManyAssociation() ? collectionLink : objectLink);
                linkedObject = new LinkedObject(objectLink);
            }
            addField(request, object, field, linkedObject, showIcons);
            
            if (field.isOneToManyAssociation()) {
                Collection collection = (Collection) field.get(object).getObject();
                if (collection.size() == 0) {
                    request.appendHtml("[empty]");
                } else {
                    // request.appendHtml(collection.size() + " elements");
                   
                    request.appendHtml("<ol>");
                    
                   for (Object element : collection) {
                       ObjectAdapter adapterFor = IsisContext.getPersistenceSession().getAdapterManager().getAdapterFor(element);
                       IsisContext.getPersistenceSession().resolveImmediately(adapterFor);

                       String id = request.getContext().mapObject(adapterFor, linkedObject.getScope(), Scope.INTERACTION);

                       request.appendHtml("<li class=\"element\">");
                       request.appendHtml("<a href=\"" + linkedObject.getForwardView() + "?" + linkedObject.getVariable() + "="
                               + id + request.getContext().encodedInteractionParameters() + "\">");
                       request.appendHtml(element.toString());
                       request.appendHtml("</a></li>");
                   }
                   request.appendHtml("</ol>");
                }
            } else {
                FieldValue.write(request, object, field, linkedObject, "value", showIcons, 0);
            }

            
            
            request.appendHtml("</div>");
        }
        request.appendHtml("</div>");
    }

    protected void addField(
            final Request request,
            final ObjectAdapter object,
            final ObjectAssociation field,
            final LinkedObject linkedObject,
            final boolean showIcons) {
     }

    @Override
    public String getName() {
        return "debug-object";
    }

}
