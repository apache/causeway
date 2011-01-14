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


package org.apache.isis.viewer.scimpi.dispatcher.view.action;

import org.apache.log4j.Logger;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.util.MethodsUtils;
import org.apache.isis.viewer.scimpi.dispatcher.view.HelpLink;

public class ActionButton extends AbstractElementProcessor {
    private static final Logger LOG = Logger.getLogger(ActionButton.class);

    public void process(Request request) {
        String objectId = request.getOptionalProperty(OBJECT);
        String methodName = request.getRequiredProperty(METHOD);
        String forwardResultTo = request.getOptionalProperty(VIEW);
        String forwardVoidTo = request.getOptionalProperty(VOID);
        String forwardErrorTo = request.getOptionalProperty(ERRORS);
        String variable = request.getOptionalProperty(RESULT_NAME);
        String scope = request.getOptionalProperty(SCOPE);
        String buttonTitle = request.getOptionalProperty(BUTTON_TITLE);
        String resultOverride = request.getOptionalProperty(RESULT_OVERRIDE);
        String idName = request.getOptionalProperty(ID);
        String className = request.getOptionalProperty(CLASS);

        ObjectAdapter object = MethodsUtils.findObject(request.getContext(), objectId);
        String version = request.getContext().mapVersion(object);
        ObjectAction action = MethodsUtils.findAction(object, methodName);

        ActionContent parameterBlock = new ActionContent(action);
        request.setBlockContent(parameterBlock);
        request.processUtilCloseTag();
        String[] parameters = parameterBlock.getParameters();
        ObjectAdapter[] objectParameters = new ObjectAdapter[parameters.length];
        int i = 0;
        for(ObjectActionParameter spec : action.getParameters()) {
            ObjectSpecification typ = spec.getSpecification();
            if (parameters[i] == null) {
                objectParameters[i] = null;
            } else if (typ.getFacet(ParseableFacet.class) != null) {
                ParseableFacet facet = (ParseableFacet) typ.getFacet(ParseableFacet.class);
                objectParameters[i] = facet.parseTextEntry(null, parameters[i]);
            } else {
                // objectParameters[i] = request.getContext().getMappedObject(parameters[i]);
                objectParameters[i] = MethodsUtils.findObject(request.getContext(), parameters[i]);
            }
            i++;
        }
        
        if (MethodsUtils.isVisibleAndUsable(object, action) && MethodsUtils.canRunMethod(object, action, objectParameters).isAllowed()) {
            // TODO use the form creation mechanism as used in ActionForm
            write(request, object, action, parameters, objectId, version, forwardResultTo, forwardVoidTo, forwardErrorTo, variable, scope, buttonTitle, resultOverride, idName, className);
        }
        request.popBlockContent();
    }

    public static void write(
            Request request,
            ObjectAdapter object,
            ObjectAction action,
            String[] parameters,
            String objectId,
            String version,
            String forwardResultTo,
            String forwardVoidTo,
            String forwardErrorTo,
            String variable,
            String scope,
            String buttonTitle, 
            String resultOverride,
            String idName,
            String className) {
        RequestContext context = request.getContext();

        buttonTitle = buttonTitle != null ? buttonTitle : action.getName();

        if (action.isVisible(IsisContext.getAuthenticationSession(), object).isVetoed()) {
            LOG.info("action not visible " + action.getName());
            return;
        }
        Consent usable = action.isUsable(IsisContext.getAuthenticationSession(), object);
        if (usable.isVetoed()) {
            LOG.info("action not available: " + usable.getReason());
            return;
        }

        /*
         * 
         * TODO this mechanism fails as it tries to process tags - which we dont need! Also it calls action
         * 'edit' (not 'action'). Field[] fields = new Field[0]; HiddenField[] hiddenFields = new
         * HiddenField[] { new HiddenField("service", serviceId), new HiddenField("method", methodName), new
         * HiddenField("view", forwardToView), variable == null ? null : new HiddenField("variable",
         * variable), }; Form.createForm(request, buttonTitle, fields, hiddenFields, false);
         */

        String idSegment = idName == null ? "" : ("id=\"" + idName + "\" ");
        String classSegment = "class=\"" + (className == null ? "button" : className) + "\"";
        request.appendHtml("\n<form " + idSegment + classSegment + " action=\"action.app\" method=\"post\">\n");
        if (objectId == null) {
            request.appendHtml("  <input type=\"hidden\" name=\"" + OBJECT + "\" value=\"" + 
                    context.getVariable(RequestContext.RESULT) + "\" />\n");
        } else {
            request.appendHtml("  <input type=\"hidden\" name=\"" + OBJECT + "\" value=\"" + objectId + "\" />\n");
        }
        request.appendHtml("  <input type=\"hidden\" name=\"" + VERSION + "\" value=\"" + version + "\" />\n");
        if (scope != null) {
            request.appendHtml("  <input type=\"hidden\" name=\"" + SCOPE + "\" value=\"" + scope + "\" />\n");
        }
        request.appendHtml("  <input type=\"hidden\" name=\"" + METHOD + "\" value=\"" + action.getId() + "\" />\n");
        if (forwardResultTo != null) {
            forwardResultTo = context.fullFilePath(forwardResultTo);
            request.appendHtml("  <input type=\"hidden\" name=\"" + VIEW + "\" value=\"" + forwardResultTo + "\" />\n");
        }
        if (forwardErrorTo == null) {
            forwardErrorTo = request.getContext().getResourceFile();
        }
        forwardErrorTo = context.fullFilePath(forwardErrorTo);
        request.appendHtml("  <input type=\"hidden\" name=\"" + ERRORS + "\" value=\"" + forwardErrorTo + "\" />\n");
        if (forwardVoidTo == null) {
            forwardVoidTo = request.getContext().getResourceFile();
        }
        forwardVoidTo = context.fullFilePath(forwardVoidTo);
        request.appendHtml("  <input type=\"hidden\" name=\"" + VOID + "\" value=\"" + forwardVoidTo + "\" />\n");
        if (variable != null) {
            request.appendHtml("  <input type=\"hidden\" name=\"" + RESULT_NAME + "\" value=\"" + variable + "\" />\n");
        }
        if (resultOverride != null) {
            request.appendHtml("  <input type=\"hidden\" name=\"" + RESULT_OVERRIDE + "\" value=\"" + resultOverride + "\" />\n");            
        }

        for (int i = 0; i < parameters.length; i++) {
            request.appendHtml("  <input type=\"hidden\" name=\"param" + (i + 1) + "\" value=\"" + parameters[i] + "\" />\n");
        }
        request.appendHtml(request.getContext().interactionFields());
        request.appendHtml("  <input class=\"button\" type=\"submit\" value=\"" + buttonTitle + "\" name=\"execute\" title=\"" 
                + action.getDescription() + "\" />");
        HelpLink.append(request, action.getDescription(), action.getHelp());
        request.appendHtml("\n</form>\n");
    }

    public String getName() {
        return "action-button";
    }

}

