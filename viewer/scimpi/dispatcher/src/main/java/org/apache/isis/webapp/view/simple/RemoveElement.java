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


package org.apache.isis.webapp.view.simple;

import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.webapp.AbstractElementProcessor;
import org.apache.isis.webapp.Dispatcher;
import org.apache.isis.webapp.ForbiddenException;
import org.apache.isis.webapp.ScimpiException;
import org.apache.isis.webapp.context.RequestContext;
import org.apache.isis.webapp.context.RequestContext.Scope;
import org.apache.isis.webapp.edit.RemoveAction;
import org.apache.isis.webapp.processor.Request;
import org.apache.isis.webapp.util.MethodsUtils;


public class RemoveElement extends AbstractElementProcessor {

    public void process(Request request) {
        String title = request.getOptionalProperty(TITLE, "Delete");
        String cls = request.getOptionalProperty(CLASS, "element-delete");
        String object = request.getOptionalProperty(OBJECT);
        String resultOverride = request.getOptionalProperty(RESULT_OVERRIDE);
        RequestContext context = request.getContext();
        String objectId = object != null ? object : (String) context.getVariable(RequestContext.RESULT);
        ObjectAdapter adapter = MethodsUtils.findObject(context, objectId);

        String element = request.getOptionalProperty(ELEMENT);
        ObjectAdapter elementId = MethodsUtils.findObject(context, element);
        
        String fieldName = request.getRequiredProperty(FIELD);
        
        String view = request.getOptionalProperty(VIEW);
        view = context.fullFilePath(view == null ? context.getResourceFile() : view);
        String error = request.getOptionalProperty(ERRORS);
        error = context.fullFilePath(error == null ? context.getResourceFile() : error);
        
        request.processUtilCloseTag();

        write(request, adapter, fieldName, elementId, resultOverride, view, error, title, cls);
    }


    public String getName() {
        return "remove-element";
    }

    public static void write(Request request, ObjectAdapter adapter, String fieldName, ObjectAdapter element, String resultOverride, String view, String error, String title, String cssClass) {
        ObjectAssociation field = adapter.getSpecification().getAssociation(fieldName);
        if (field == null) {
            throw new ScimpiException("No field " + fieldName + " in " + adapter.getSpecification().getFullName());
        }
        if (field.isVisible(IsisContext.getAuthenticationSession(), adapter).isVetoed()) {
            throw new ForbiddenException("Field " + fieldName + " in " + adapter.getSpecification().getFullName() + " is not visible");
        }
        IsisContext.getPersistenceSession().resolveField(adapter, field);

        if (valid(request, adapter)) {
            String classSegment = " class=\"" + cssClass + "\"";

            String objectId = request.getContext().mapObject(adapter, Scope.INTERACTION);
            String elementId = request.getContext().mapObject(element, Scope.INTERACTION);
            String action = RemoveAction.ACTION + Dispatcher.COMMAND_ROOT;
            request.appendHtml("<form" + classSegment + " method=\"POST\" action=\"" + action + "\" >");
            request.appendHtml("<input type=\"hidden\" name=\"" + OBJECT + "\" value=\"" + objectId + "\" />");
            request.appendHtml("<input type=\"hidden\" name=\"" + FIELD + "\" value=\"" + fieldName + "\" />");
            request.appendHtml("<input type=\"hidden\" name=\"" + ELEMENT + "\" value=\"" + elementId + "\" />");
            if (resultOverride != null) {
                request.appendHtml("<input type=\"hidden\" name=\"" + RESULT_OVERRIDE + "\" value=\"" + resultOverride + "\" />");
            }
            request.appendHtml("<input type=\"hidden\" name=\"" + VIEW + "\" value=\"" + view + "\" />");
            request.appendHtml("<input type=\"hidden\" name=\"" + ERRORS + "\" value=\"" + error + "\" />");
            request.appendHtml("<input class=\"button\" type=\"submit\" value=\"" + title + "\" />");
            request.appendHtml("</form>");
        } 
    }
    
    private static boolean valid(Request request, ObjectAdapter adapter) {
        // TODO check is valid to remove element
        AuthenticationSession session = IsisContext.getAuthenticationSession();
        Filter<ObjectAssociation> filter = ObjectAssociationFilters.dynamicallyVisible(session, adapter);
        ObjectAssociation[] visibleFields = adapter.getSpecification().getAssociations(filter);
        return visibleFields.length > 0;
    }
}

