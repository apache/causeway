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


package org.apache.isis.viewer.scimpi.dispatcher.view.simple;

import java.util.List;

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.ForbiddenException;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.edit.RemoveAction;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.util.MethodsUtils;


public class RemoveElement extends AbstractElementProcessor {

    @Override
    public void process(Request request) {
        String title = request.getOptionalProperty(BUTTON_TITLE, "Remove From List");
        String cls = request.getOptionalProperty(CLASS, "action in-line delete");
        String object = request.getOptionalProperty(OBJECT);
        String resultOverride = request.getOptionalProperty(RESULT_OVERRIDE);
        RequestContext context = request.getContext();
        String objectId = object != null ? object : (String) context.getVariable(RequestContext.RESULT);
        ObjectAdapter adapter = MethodsUtils.findObject(context, objectId);

        String element = request.getOptionalProperty(ELEMENT, (String) context.getVariable(ELEMENT));
        ObjectAdapter elementId = MethodsUtils.findObject(context, element);
        
        String fieldName = request.getRequiredProperty(FIELD);
        
        String view = request.getOptionalProperty(VIEW);
        view = context.fullFilePath(view == null ? context.getResourceFile() : view);
        String error = request.getOptionalProperty(ERRORS);
        error = context.fullFilePath(error == null ? context.getResourceFile() : error);
        
        request.processUtilCloseTag();

        write(request, adapter, fieldName, elementId, resultOverride, view, error, title, cls);
    }


    @Override
    public String getName() {
        return "remove-element";
    }

    public static void write(Request request, ObjectAdapter adapter, String fieldName, ObjectAdapter element, String resultOverride, String view, String error, String title, String cssClass) {
        ObjectAssociation field = adapter.getSpecification().getAssociation(fieldName);
        if (field == null) {
            throw new ScimpiException("No field " + fieldName + " in " + adapter.getSpecification().getFullIdentifier());
        }
        if (!field.isOneToManyAssociation()) {
            throw new ScimpiException("Field " + fieldName + " not a collection, in " + adapter.getSpecification().getFullIdentifier());
        }
        if (field.isVisible(IsisContext.getAuthenticationSession(), adapter).isVetoed()) {
            throw new ForbiddenException(field, ForbiddenException.VISIBLE);
        }
        IsisContext.getPersistenceSession().resolveField(adapter, field);

        
        Consent usable = field.isUsable(IsisContext.getAuthenticationSession(), adapter);
        if (usable.isAllowed()) {
            usable = ((OneToManyAssociation) field).isValidToRemove(adapter, element);
        }
        
        if (usable.isVetoed()) {
            request.appendHtml("<span class=\"veto\">" + usable.getReason() + "</span>");
        } else {
            if (valid(request, adapter)) {
                String classSegment = " class=\"" + cssClass + "\"";
    
                String objectId = request.getContext().mapObject(adapter, Scope.INTERACTION);
                String elementId = request.getContext().mapObject(element, Scope.INTERACTION);
                String action = RemoveAction.ACTION + Dispatcher.COMMAND_ROOT;
                request.appendHtml("<form" + classSegment + " method=\"post\" action=\"" + action + "\" >");
                request.appendHtml("<input type=\"hidden\" name=\"" + OBJECT + "\" value=\"" + objectId + "\" />");
                request.appendHtml("<input type=\"hidden\" name=\"" + FIELD + "\" value=\"" + fieldName + "\" />");
                request.appendHtml("<input type=\"hidden\" name=\"" + ELEMENT + "\" value=\"" + elementId + "\" />");
                if (resultOverride != null) {
                    request.appendHtml("<input type=\"hidden\" name=\"" + RESULT_OVERRIDE + "\" value=\"" + resultOverride + "\" />");
                }
                request.appendHtml("<input type=\"hidden\" name=\"" + VIEW + "\" value=\"" + view + "\" />");
                request.appendHtml("<input type=\"hidden\" name=\"" + ERRORS + "\" value=\"" + error + "\" />");
                request.appendHtml(request.getContext().interactionFields());
                request.appendHtml("<input class=\"button\" type=\"submit\" value=\"" + title + "\" />");
                request.appendHtml("</form>");
            } 
        }
    }
    
    private static boolean valid(Request request, ObjectAdapter adapter) {
        // TODO is this check valid/necessary?
        
        // TODO check is valid to remove element
        AuthenticationSession session = IsisContext.getAuthenticationSession();
        Filter<ObjectAssociation> filter = ObjectAssociationFilters.dynamicallyVisible(session, adapter);
        List<ObjectAssociation> visibleFields = adapter.getSpecification().getAssociations(filter);
        if (visibleFields.size() == 0) {
            return false;
        }
        
        
        
        return true;
    }
}

