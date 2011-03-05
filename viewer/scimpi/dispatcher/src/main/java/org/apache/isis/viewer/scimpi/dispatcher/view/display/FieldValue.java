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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.progmodel.facets.value.booleans.BooleanValueFacet;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ForbiddenException;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.view.field.LinkedObject;


public class FieldValue extends AbstractElementProcessor {

    public void process(Request request) {
        String className = request.getOptionalProperty(CLASS);
        String id = request.getOptionalProperty(OBJECT);
        String fieldName = request.getRequiredProperty(FIELD);
        ObjectAdapter object = request.getContext().getMappedObjectOrResult(id);
        ObjectAssociation field = object.getSpecification().getAssociation(fieldName);
        if (field == null) {
            throw new ScimpiException("No field " + fieldName + " in " + object.getSpecification().getFullIdentifier());
        }
        if (field.isVisible(IsisContext.getAuthenticationSession(), object).isVetoed()) {
            throw new ForbiddenException(field, ForbiddenException.VISIBLE);
        }
        boolean isIconShowing = request.isRequested(SHOW_ICON, true);
        String truncateTo = request.getOptionalProperty(TRUNCATE, "0");

        write(request, (ObjectAdapter) object, field, null, className, isIconShowing, Integer.valueOf(truncateTo).intValue());
    }

    public String getName() {
        return "field";
    }

    public static void write(
            Request request,
            ObjectAdapter object,
            ObjectAssociation field,
            LinkedObject linkedField,
            String className,
            boolean showIcon, int truncateTo) {

        ObjectAdapter fieldReference = field.get(object);

        if (fieldReference != null) {
            String classSection = "class=\"" + (className == null ? "field" : className)  + "\""; 
            request.appendHtml("<span " + classSection + ">"); 
            if (field.isOneToOneAssociation()) {
                IsisContext.getPersistenceSession().resolveImmediately((ObjectAdapter) fieldReference);
            }

            if (!field.getSpecification().containsFacet(ParseableFacet.class) && showIcon) {
                request.appendHtml("<img class=\"small-icon\" src=\"" + request.getContext().imagePath(fieldReference)
                        + "\" alt=\"" + field.getSpecification().getShortIdentifier() + "\"/>");
            }
            
            if (linkedField != null) {
                String id = request.getContext().mapObject((ObjectAdapter) fieldReference, linkedField.getScope(), Scope.INTERACTION);
                request.appendHtml("<a href=\"" + linkedField.getForwardView() + "?" + linkedField.getVariable() + "="
                        + id + request.getContext().encodedInteractionParameters() + "\">");
            }
            String value = fieldReference == null ? "" : fieldReference.titleString();
            if (truncateTo > 0 && value.length() > truncateTo) {
                value = value.substring(0, truncateTo) + "...";
            }
            
            // TODO figure out a better way to determine if boolean or a password
            ObjectSpecification spec = field.getSpecification();
            BooleanValueFacet facet =  (BooleanValueFacet) spec.getFacet(BooleanValueFacet.class);
            if (facet != null) {
                boolean flag = facet.isSet(fieldReference);
                String valueSegment =  flag ? " checked=\"checked\"" : "";
                String disabled = " disabled=\"disabled\"";
                value =  "<input type=\"checkbox\"" + valueSegment + disabled + " />";
            }
            
            request.appendHtml(value);
            if (linkedField != null) {
                request.appendHtml("</a>");
            }
            request.appendHtml("</span>");
        }
    }

}

